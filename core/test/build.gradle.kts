plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:domain"))
    api(libs.kotest.runner.junit5)
    api(libs.kotest.assertions.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlin.result)
}
