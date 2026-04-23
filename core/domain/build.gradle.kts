plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jvm)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(project(":core:test"))

    implementation(libs.kotlin.result)
}
