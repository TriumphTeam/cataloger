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
package dev.triumphteam.cataloger

import dev.triumphteam.cataloger.task.ValidateCatalogTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.plugins.catalog.internal.DefaultVersionCatalogPluginExtension
import org.gradle.kotlin.dsl.register
import java.nio.file.Path

/**
 * This class represents a cataloger plugin for Gradle projects.
 *
 * Responsibilities:
 * - Importing and combining toml catalog files
 * - Registering a validator for the catalog
 */
public abstract class CatalogerPlugin : Plugin<Project> {

    private companion object {
        private const val GRADLE_CATALOG_PLUGIN = "org.gradle.version-catalog"
        private const val VALIDATE_CATALOG_TASK = "validateCatalog"
        private const val CATALOG_PATH = "libs.versions.toml"
    }

    override fun apply(project: Project): Unit = with(project) {
        // Apply the catalog plugin
        plugins.apply(GRADLE_CATALOG_PLUGIN)

        val catalogExtension = extensions.getByType(CatalogPluginExtension::class.java)

        afterEvaluate {
            catalogExtension.versionCatalog {
                // Importing root toml first
                TomlCatalogFileParser.parse(rootProject.tomlCatalog, this)
                // We don't want to import root twice
                if (project == rootProject) return@versionCatalog
                // Then combining it with the actual project's toml
                TomlCatalogFileParser.parse(project.tomlCatalog, this)
            }
        }

        if (catalogExtension is DefaultVersionCatalogPluginExtension) {
            // Register validator
            tasks.register<ValidateCatalogTask>(VALIDATE_CATALOG_TASK) {
                this.catalogExtension.set(catalogExtension)
            }
        }
    }

    /** Quick util for getting the catalog file. */
    private val Project.tomlCatalog: Path
        get() = file(CATALOG_PATH).toPath()
}
