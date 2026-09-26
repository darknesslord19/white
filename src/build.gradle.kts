plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.örnekfilm"
    compileSdk = 34
}

cloudstream {
    language = "tr"
    description = "ÖrnekFilm CloudStream eklentisi"
    authors = listOf("Anonim")
}

dependencies {
    implementation("com.github.recloudstream:cloudstream:master-SNAPSHOT")
}
