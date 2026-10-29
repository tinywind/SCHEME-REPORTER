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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.tinywind.schemereporter.sample.Creator
import java.io.File
import java.nio.file.Files

@RunWith(Parameterized::class)
class SchemeReporterPluginFunctionalTest(
    private val gradleVersion: String,
    private val javaHome: String,
    private val label: String,
) {

    companion object {
        private val sdkmanDir = System.getenv("SDKMAN_DIR")
            ?: "${System.getProperty("user.home")}/.sdkman"

        private lateinit var h2Server: Server
        private var h2Port: Int = 0

        private fun jdkPath(prefix: String): String? {
            val candidates = File("$sdkmanDir/candidates/java")
            if (!candidates.exists()) return null
            return candidates.listFiles()
                ?.filter { it.isDirectory && it.name != "current" && it.name.startsWith(prefix) }
                ?.maxByOrNull { it.name }
                ?.absolutePath
        }

        @JvmStatic
        @BeforeClass
        fun startH2() {
            Creator.create()

            h2Server = Server.createTcpServer("-tcpPort", "0", "-tcpAllowOthers").start()
            h2Port = h2Server.port
        }

        @JvmStatic
        @AfterClass
        fun stopH2() {
            h2Server.stop()
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun parameters(): Collection<Array<Any>> {
            val params = mutableListOf<Array<Any>>()

            val jdk21 = jdkPath("21")
            val jdk25 = jdkPath("25")

            if (jdk21 != null) {
                params.add(arrayOf("8.6", jdk21, "Gradle 8.6 + JDK 21"))
                params.add(arrayOf("9.8.2", jdk21, "Gradle 9.8.2 + JDK 21"))
            }
            if (jdk25 != null) {
                params.add(arrayOf("9.8.2", jdk25, "Gradle 9.8.2 + JDK 25"))
            }

            require(params.isNotEmpty()) { "No JDK found. Install JDK 21 or 25 via SDKMAN." }
            return params
        }
    }

    private lateinit var projectDir: File
    private lateinit var outputDir: File

    @Before
    fun setup() {
        projectDir = Files.createTempDirectory("scheme-reporter-test-").toFile()
        outputDir = File(projectDir, "build/reports/schema")

        File(projectDir, "settings.gradle.kts").writeText(
            "rootProject.name = \"scheme-reporter-test\"\n"
        )

        val pluginClasspath = buildPluginClasspath()
        val escapedOutputDir = outputDir.absolutePath.replace("\\", "/")

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
                    url = "jdbc:h2:tcp://localhost:$h2Port/mem:test"
                    user = "sa"
                    password = ""
                    includes = ".*"
                    inputSchema = "PUBLIC"
                }
                generator {
                    reporterClass = "org.tinywind.schemereporter.pdf.PdfReporter"
                    outputDirectory = "$escapedOutputDir"
                }
            }
            """.trimIndent()
        )
    }

    @After
    fun cleanup() {
        projectDir.deleteRecursively()
    }

    @Test
    fun `generateSchemeReport task succeeds`() {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateSchemeReport", "--stacktrace")
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
            .build()

        assertEquals(
            "Task should succeed with $label",
            TaskOutcome.SUCCESS,
            result.task(":generateSchemeReport")?.outcome,
        )
        assertTrue("Output directory should exist", outputDir.exists())

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

    private fun buildPluginClasspath(): List<String> {
        val resource = javaClass.classLoader.getResource("plugin-under-test-metadata.properties")
            ?: throw IllegalStateException("plugin-under-test-metadata.properties not found")

        val props = java.util.Properties()
        resource.openStream().use { props.load(it) }
        return props.getProperty("implementation-classpath")
            .split(File.pathSeparator)
    }
}
