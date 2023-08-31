plugins {
    id("com.github.hierynomus.license") version "0.16.1"
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.9.10"
    `java-gradle-plugin`
    `kotlin-dsl`

}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleTestKit())
    implementation(kotlin("stdlib"))
}

kotlin {
    explicitApi()
}

license {
    header = rootProject.file("LICENSE")
    encoding = "UTF-8"
    useDefaultMappings = true

    include("**/*.kt")
}

gradlePlugin {
    website.set("https://github.com/TriumphTeam/cataloger")
    vcsUrl.set("https://github.com/TriumphTeam/cataloger.git")

    plugins {
        create("cataloger") {
            id = "dev.triumphteam.cataloger"
            displayName = "Cataloger"
            description = "Join and validate version catalogs before publishing them."
            tags.set(listOf("version-catalog"))
            implementationClass = "dev.triumphteam.cataloger.CatalogerPlugin"
        }
    }
}
