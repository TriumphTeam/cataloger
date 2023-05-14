dependencyResolutionManagement {
    includeBuild("build-logic")
    repositories.gradlePluginPortal()
}

val projectName = "catalog"
rootProject.name = "catalog-common"

listOf(
    "root",
    "lily",
    "service",
    "application"
).forEach(::includeProject)

fun includeProject(name: String) {
    include(name) {
        this.name = "$projectName-$name"
    }
}

fun include(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
