/**
 * MIT License
 *
 * Copyright (c) 2019-2023 Matt (@LichtHund) - TriumphTeam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.cataloger.task

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.plugins.catalog.internal.DefaultVersionCatalogPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.lang.StringBuilder
import kotlin.io.path.createTempDirectory

/** A task that validates a catalog by setting up and running Gradle test files. */
public abstract class ValidateCatalogTask : DefaultTask() {

    private companion object {
        private const val GRADLE_SETTINGS_FILE_NAME = "settings.gradle.kts"
        private const val GRADLE_BUILD_FILE_NAME = "build.gradle.kts"
        private const val FAILED_DEPENDENCY_TRIGGER = "FAILED"
    }

    /** Property containing the imported already combined catalog. */
    @get:Input
    public abstract val catalogExtension: Property<DefaultVersionCatalogPluginExtension>

    /** Validates the catalog by creating and setting up the necessary gradle test files. */
    @TaskAction
    public fun validate() {
        // Temporary project directory
        val directory = createTempDirectory().toFile()

        // Create and set up the gradle test files
        File(directory, GRADLE_SETTINGS_FILE_NAME).writeText("rootProject.name = \"cataloger-${project.name}\"")
        File(directory, GRADLE_BUILD_FILE_NAME).writeBuild()

        val result = GradleRunner.create()
            .withProjectDir(directory)
            // Run with the dependencies task
            .withArguments("dependencies")
            .build()

        val failedDependencies = result.output.lines().filter { FAILED_DEPENDENCY_TRIGGER in it }

        // Success we don't do anything
        if (failedDependencies.isEmpty()) return

        throw InvalidDependencyException(failedDependencies)
    }

    /**
     * Writes the build script for a file.
     *
     * The function generates a basic Gradle build script.
     * It uses the repositories and version catalog defined in the project to populate the script.
     * The generated script sets the Java language version to 17.
     */
    private fun File.writeBuild() {
        val repositories = project.repositories.filterIsInstance<MavenArtifactRepository>()
        val catalogue = catalogExtension.get().versionCatalog.get()

        // Generate basic build script
        val gradleBuildScript = """
            plugins {
                `java-library`
            }
            
            ${repositories.toRepositoriesBlock(16)}
            
            ${catalogue.toDependenciesBlock(16)}
            
            java {
                toolchain.languageVersion.set(JavaLanguageVersion.of(17))
            }
        """.trimIndent()

        // Write it to the file
        writeText(gradleBuildScript)
    }

    /**
     * Converts a list of Maven artifact repositories to a Gradle repositories block.
     *
     * @param startIndent The number of spaces to indent the repositories block.
     * @return The Gradle repositories block as a string.
     */
    private fun List<MavenArtifactRepository>.toRepositoriesBlock(startIndent: Int): String {
        return buildString {
            appendLine("repositories {")
            this@toRepositoriesBlock.forEach { repo ->
                append(startIndent + 4, "maven(\"${repo.url}\")")

                val credentials = repo.credentials
                val username = credentials.username
                val password = credentials.password

                if (username == null && password == null) {
                    appendLine()
                    return@forEach
                }

                appendLine(" {")
                appendLine(startIndent + 8, "credentials {")
                if (username != null) appendLine(startIndent + 12, "username = \"$username\"")
                if (username != null) appendLine(startIndent + 12, "password = \"$password\"")
                appendLine(startIndent + 8, "}")
                appendLine(startIndent + 4, "}")
            }
            appendLine(startIndent,"}")
        }
    }

    /**
     * Generates a dependencies block for the given DefaultVersionCatalog.
     *
     * @param startIndent The number of spaces to indent the dependencies block.
     * @return The dependencies block as a String.
     */
    private fun DefaultVersionCatalog.toDependenciesBlock(startIndent: Int): String {
        val dependencies = libraryAliases.map(::getDependencyData)
        return buildString {
            appendLine("dependencies {")
            dependencies.forEach { model ->
                append(startIndent + 4, "compileOnly(\"")
                append(model.group).append(":")
                append(model.name).append(":")
                append(model.version)
                appendLine("\")")
            }
            appendLine(startIndent,"}")
        }
    }

    /**
     * Appends a new line with a specified indentation level to the StringBuilder.
     *
     * @param indent The number of spaces to indent the line.
     * @param line The line to be appended.
     */
    private fun StringBuilder.appendLine(indent: Int, line: String) {
        appendLine(" ".repeat(indent) + line)
    }

    /**
     * Appends the given text to this StringBuilder with the specified indentation level.
     *
     * @param indent the number of spaces to indent the text
     * @param text the text to be appended
     */
    private fun StringBuilder.append(indent: Int, text: String) {
        append(" ".repeat(indent) + text)
    }
}

/** This class represents an exception that is thrown when invalid dependencies are found. */
public class InvalidDependencyException(errorLines: List<String>) : RuntimeException(
    """
        Found invalid dependencies, check groupId, artifactId, version, version.ref, or missing repository.
        Dependencies:
            ${errorLines.joinToString("\n            ") { it.trimIndent().trim() }} 
    """.trimIndent()
)
