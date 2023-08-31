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
            description = "Yeah"
            // tags.set(listOf("tags", "for", "your", "plugins"))
            implementationClass = "dev.triumphteam.cataloger.CatalogerPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("REPO_USER")
                password = System.getenv("REPO_PASS")
            }

            url = uri("https://repo.triumphteam.dev/snapshots/")
        }
    }
}
