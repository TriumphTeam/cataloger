package dev.triumphteam.cataloguer

import dev.triumphteam.cataloguer.task.ValidateCatalogTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import org.gradle.api.plugins.catalog.internal.DefaultVersionCatalogPluginExtension
import org.gradle.kotlin.dsl.register
import java.nio.file.Path

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
