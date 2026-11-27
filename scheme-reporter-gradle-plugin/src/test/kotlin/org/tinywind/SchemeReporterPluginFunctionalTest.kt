package org.tinywind

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.h2.tools.Server
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.tinywind.schemereporter.sample.Creator
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.jar.JarOutputStream
import java.util.jar.JarEntry

@RunWith(Parameterized::class)
class SchemeReporterPluginFunctionalTest(
    private val gradleVersion: String,
    private val javaHome: String,
    private val javaVersion: Int,
) {

    companion object {
        private lateinit var h2Server: Server
        private var h2Port: Int = 0

        private fun javaVersion(home: File): Int? {
            val release = File(home, "release")
            if (!release.isFile) return null
            val properties = java.util.Properties()
            release.inputStream().use { properties.load(it) }
            return properties.getProperty("JAVA_VERSION")?.trim('"')
                ?.takeWhile { it.isDigit() }?.toIntOrNull()
        }

        @JvmStatic
        @BeforeClass
        fun startH2() {
            Creator.create()

            h2Server = Server.createTcpServer("-tcpPort", "0").start()
            h2Port = h2Server.port
        }

        @JvmStatic
        @AfterClass
        fun stopH2() {
            h2Server.stop()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "Gradle {0} + JDK {2}")
        fun parameters(): Collection<Array<Any>> {
            val requestedHome = System.getenv("SCHEME_REPORTER_TEST_JAVA_HOME")
            val homes = if (requestedHome != null) {
                listOf(File(requestedHome))
            } else {
                val sdkman = System.getenv("SDKMAN_DIR") ?: "${System.getProperty("user.home")}/.sdkman"
                listOfNotNull(
                    System.getProperty("java.home"), System.getenv("JAVA_HOME"),
                    System.getenv("JAVA21_HOME"), System.getenv("JAVA25_HOME"),
                ).map(::File) + File(sdkman, "candidates/java").listFiles().orEmpty().sortedByDescending { it.name }
            }
            val params = mutableListOf<Array<Any>>()
            for (version in listOf(21, 25)) {
                val home = homes.firstOrNull { javaVersion(it) == version } ?: continue
                val gradleVersions = if (version == 21) listOf("8.6", "9.9.1") else listOf("9.9.1")
                for (gradle in gradleVersions) params.add(arrayOf(gradle, home.absolutePath, version))
            }
            require(params.isNotEmpty()) { "Provide a JDK 21 or 25 installation via SCHEME_REPORTER_TEST_JAVA_HOME." }
            return params
        }
    }

    private lateinit var projectDir: File
    private lateinit var outputDir: File

    @Before
    fun setup() {
        val scratch = Files.createDirectories(File("../.tmp").toPath().toAbsolutePath().normalize())
        projectDir = Files.createTempDirectory(scratch, "scheme-reporter-test-").toFile()
        outputDir = File(projectDir, "build/reports/schema")

        File(projectDir, "settings.gradle.kts").writeText(
            "rootProject.name = \"scheme-reporter-test\"\n"
        )

        File(projectDir, "gradle.properties").writeText(
            "org.gradle.java.home=${File(javaHome).invariantSeparatorsPath}\norg.gradle.jvmargs=-Xmx1g\nh2Port=$h2Port\n"
        )
        val pluginClasspath = buildPluginClasspath()

        File(projectDir, "build.gradle.kts").writeText(
            """
            buildscript {
                repositories {
                    mavenLocal()
                    mavenCentral()
                }
                dependencies {
                    ${pluginClasspath.joinToString("\n                    ") { "classpath(files(\"${it.replace("\\", "/")}\"))" }}
                }
            }

            apply(plugin = "org.tinywind.scheme-reporter")
            check(Runtime.version().feature() == $javaVersion) { "Unexpected consumer JDK: ${'$'}{Runtime.version()}" }
            check(java.io.ObjectInputFilter.Config.getSerialFilter() != null) { "Missing build serialization filter" }

            repositories {
                mavenLocal()
                mavenCentral()
            }

            dependencies {
                "schemaReporter"("com.h2database:h2:2.5.252")
            }

            configure<org.tinywind.SchemeReporterExtension> {
                jdbc {
                    driverClass = "org.h2.Driver"
                }
                database {
                    url = "jdbc:h2:tcp://localhost:${'$'}{providers.gradleProperty("h2Port").get()}/mem:test"
                    user = "sa"
                    password = ""
                    includes = "PUBLIC_FILE"
                    inputSchema = "PUBLIC"
                }
                generator {
                    providers.gradleProperty("reportFormat").orNull?.let { reporterClass = it }
                    outputDirectory = layout.buildDirectory.dir("reports/schema").get().asFile.absolutePath
                }
            }
            """.trimIndent()
        )
    }

    @After
    fun cleanup() {
        projectDir.deleteRecursively()
    }

    private fun runner(vararg options: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateSchemeReport", "--stacktrace", *options)
            .withGradleVersion(gradleVersion)
            .withEnvironment(
                System.getenv().toMutableMap().apply {
                    put("JAVA_HOME", javaHome)
                    put("JAVA_TOOL_OPTIONS", listOfNotNull(
                        get("JAVA_TOOL_OPTIONS"),
                        "-Djdk.serialFilter=${System.getProperty("jdk.serialFilter")}",
                    ).joinToString(" "))
                }
            )
            .forwardOutput()

    private fun generate(vararg options: String) {
        val result = runner(*options).build()

        assertEquals(
            "Task should succeed with Gradle $gradleVersion on JDK $javaVersion",
            TaskOutcome.SUCCESS,
            result.task(":generateSchemeReport")?.outcome,
        )
    }

    @Test
    fun `default format generates a PDF report`() {
        generate()

        val pdfFiles = outputDir.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
        assertTrue("At least one PDF report should be generated", pdfFiles.isNotEmpty())
        PdfDocument(PdfReader(pdfFiles.first().absolutePath)).use { pdf ->
            assertTrue("Report should have pages", pdf.numberOfPages > 0)
            val text = (1..pdf.numberOfPages).joinToString("\n") { page ->
                PdfTextExtractor.getTextFromPage(pdf.getPage(page))
            }
            assertTrue("Report should describe the actual sample table", text.contains("PUBLIC_FILE"))
        }
    }

    @Test
    fun `HTML alias and class name generate HTML reports`() {
        for (format in listOf("html", "org.tinywind.schemereporter.html.HtmlReporter")) {
            generate("-PreportFormat=$format")
            val report = outputDir.listFiles().orEmpty().single { it.extension == "html" }
            assertTrue("HTML report for $format should describe the sample table", report.readText().contains("PUBLIC_FILE"))
            assertTrue("Remove report before checking the next format", report.delete())
        }
    }

    @Test
    fun `driver class loader releases its JAR on success and failure`() {
        val marker = File(projectDir, "private-driver.jar")
        val resource = "driver-lifecycle.marker"
        JarOutputStream(marker.outputStream()).use { jar ->
            jar.putNextEntry(JarEntry(resource))
            jar.closeEntry()
        }

        lateinit var owner: URLClassLoader
        fun openMarker(loader: URLClassLoader) {
            owner = loader
            assertNotNull(loader.findResource(resource))
        }

        withDriverClassLoader(listOf(marker), ::openMarker)
        assertNull(owner.findResource(resource))
        val failure = IllegalStateException("generation failed")
        val thrown = assertThrows(IllegalStateException::class.java) {
            withDriverClassLoader(listOf(marker)) { loader ->
                openMarker(loader)
                throw failure
            }
        }
        assertEquals(failure, thrown)
        assertNull(owner.findResource(resource))

        val descriptors = File("/proc/self/fd")
        if (descriptors.isDirectory) {
            assertTrue(descriptors.listFiles()!!.none { it.canonicalFile == marker.canonicalFile })
        }
        assertTrue("Cannot delete the JDBC driver JAR", marker.delete())
    }

    private fun buildPluginClasspath(): List<String> {
        val resource = javaClass.classLoader.getResource("plugin-under-test-metadata.properties")
            ?: throw IllegalStateException("plugin-under-test-metadata.properties not found")

        val props = java.util.Properties()
        resource.openStream().use { props.load(it) }
        return props.getProperty("implementation-classpath")
            .split(File.pathSeparator)
    }
}
