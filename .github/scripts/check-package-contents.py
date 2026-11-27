from pathlib import Path
from xml.etree import ElementTree
from zipfile import ZipFile


root = Path(__file__).resolve().parents[2]
version = ElementTree.parse(root / "pom.xml").findtext("{*}version")
project_license = (root / "LICENSE.txt").read_bytes()
core = "scheme-reporter"
standalone = "scheme-reporter-standalone"
plugin = "scheme-reporter-gradle-plugin"
font_directory = root / core / "src/main/resources/asset/font"


def require_contents(jar, entry, expected):
    if jar.read(entry) != expected:
        raise ValueError(f"{jar.filename}: incorrect {entry}")


def check_fonts(jar):
    fonts = {name for name in jar.namelist() if name.startswith("asset/font/") and name.endswith(".ttf")}
    if fonts != {"asset/font/NanumMyeongjo.ttf"}:
        raise ValueError(f"{jar.filename}: unexpected font inventory {fonts}")
    for filename in ("NanumMyeongjo.ttf", "OFL.txt"):
        require_contents(jar, f"asset/font/{filename}", (font_directory / filename).read_bytes())


for module in (core, "scheme-reporter-maven", "scheme-reporter-sample-database", standalone, plugin):
    output = "build/libs" if module == plugin else "target"
    with ZipFile(root / module / output / f"{module}-{version}.jar") as jar:
        require_contents(jar, "META-INF/LICENSE.txt", project_license)
        if module == core:
            check_fonts(jar)
        if module == plugin:
            descriptor = "META-INF/gradle-plugins/org.tinywind.scheme-reporter.properties"
            entries = [
                name for name in jar.namelist()
                if name.startswith("META-INF/gradle-plugins/") and name.endswith(".properties")
            ]
            if entries != [descriptor]:
                raise ValueError(f"{jar.filename}: unexpected plugin descriptors {entries}")
            implementation = [line for line in jar.read(descriptor).splitlines() if line.startswith(b"implementation-class=")]
            if implementation != [b"implementation-class=org.tinywind.SchemeReporterPlugin"]:
                raise ValueError(f"{jar.filename}: incorrect plugin implementation {implementation}")


with ZipFile(root / standalone / "target" / f"{standalone}-{version}-jar-with-dependencies.jar") as jar:
    require_contents(jar, "META-INF/licenses/scheme-reporter/LICENSE.txt", project_license)
    if jar.read("META-INF/LICENSE.txt") == project_license:
        raise ValueError(f"{jar.filename}: project notice replaced the dependency license")
    check_fonts(jar)
    entries = set(jar.namelist())
    required = {
        "org/tinywind/schemereporter/standalone/Launcher.class",
        "org/tinywind/schemereporter/SchemeReporter.class",
        "asset/default.html",
        "xsd/scheme-reporter-0.1.xsd",
    }
    missing = required - entries
    if missing:
        raise ValueError(f"{jar.filename}: missing runtime entries {sorted(missing)}")
    removed = {"barcodes", "font-asian", "forms", "hyph", "pdfa", "sign"}
    forbidden = {f"META-INF/maven/com.itextpdf/{module}/pom.properties" for module in removed}
    forbidden.add("org/apache/tools/ant/Main.class")
    unexpected = entries & forbidden
    if unexpected:
        raise ValueError(f"{jar.filename}: found unused dependencies {sorted(unexpected)}")
