import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.guguma.guguma_application"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guguma.guguma_application"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        // local.properties 파일 읽기
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        // 기본 URL 및 엔드포인트를 BuildConfig에 추가
        val baseUrl = localProperties.getProperty("api.aws.base.url", "http://localhost:8080/api")
        val detectPath = localProperties.getProperty("api.key.plant.detect")
        val recognizePath = localProperties.getProperty("api.key.plant.recognize", "/plants/recognize")
        val registerPath = localProperties.getProperty("api.key.plant.register", "/plants/register")
        val localbaseUrl = localProperties.getProperty("api.local.base.url")

        buildConfigField("String", "API_BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "API_PLANT_DETECT", "\"$detectPath\"")
        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$baseUrl$recognizePath\"")
//        buildConfigField("String", "API_PLANT_REGISTER", "\"$baseUrl$registerPath\"")
//        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$localbaseUrl$recognizePath\"")
        buildConfigField("String", "API_PLANT_REGISTER", "\"$localbaseUrl$registerPath\"")
    }

    buildFeatures {
        buildConfig = true
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}