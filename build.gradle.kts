plugins {
    kotlin("jvm") version "1.9.10"
    `java-gradle-plugin`
    `kotlin-dsl`
}

dependencies {
    implementation(gradleTestKit())
}

kotlin {
    explicitApi()
}
