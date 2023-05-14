import plugin.CatalogJoinerPlugin

apply<CatalogJoinerPlugin>()

plugins {
    `version-catalog`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/TriumphCraft/catalog/")

            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
