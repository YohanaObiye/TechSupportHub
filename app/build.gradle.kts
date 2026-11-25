plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.techsupporthub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.techsupporthub"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.1")) // Check for the latest version
//    implementation("io.github.jan-tennert.supabase:postgrest-kt")
//    implementation("io.ktor:ktor-client-android:2.3.3") // Check for the latest version
//    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.1")


    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Add the Retrofit library
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Add the Volley library
    implementation("com.android.volley:volley:1.2.0")

// Add the OkHttp library
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")


}