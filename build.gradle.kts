// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

dependencies {
    // Google Drive REST API
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
    }

    // Auth & Identity
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
}