package plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.catalog.parser.TomlCatalogFileParser
import org.gradle.api.plugins.catalog.CatalogPluginExtension
import java.nio.file.Path

/** Allows joining catalog files into a combination. */
class CatalogJoinerPlugin : Plugin<Project> {

    private companion object {
        private const val CATALOG_PATH = "libs.versions.toml"
    }

    override fun apply(project: Project) = with(project) {
        // Apply the catalog plugin so we can use it
        plugins.apply("org.gradle.version-catalog")

        afterEvaluate {
            extensions.getByType(CatalogPluginExtension::class.java).versionCatalog {
                TomlCatalogFileParser.parse(rootProject.versionCatalog, this)
                // We don't want to import root twice
                if (project == rootProject) return@versionCatalog
                TomlCatalogFileParser.parse(project.versionCatalog, this)
            }
        }
    }

    /** Quick util for getting the catalog file. */
    private val Project.versionCatalog: Path
        get() = file(CATALOG_PATH).toPath()
}
