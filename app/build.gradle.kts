import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
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
        val localbaseUrl = localProperties.getProperty("api.local.base.url")

        val detectPath = localProperties.getProperty("api.key.plant.detect")
        val recognizePath =
            localProperties.getProperty("api.key.plant.recognize", "/plants/recognize")
        val registerPath = localProperties.getProperty("api.key.plant.register", "/plants/register")
        val plantlistPath = localProperties.getProperty("api.key.plant.plantlist", "/plants/user/1")
        val plantlistdeletePath =
            localProperties.getProperty("api.key.plant.plantlistdelete", "/plants/{id}")

        // 로컬 테스트용
        buildConfigField("String", "API_BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$localbaseUrl$recognizePath\"")
        buildConfigField("String", "API_PLANT_REGISTER", "\"$localbaseUrl$registerPath\"")
        buildConfigField("String", "API_PLANT_LIST", "\"$localbaseUrl$plantlistPath\"")
        buildConfigField("String", "API_PLANT_DELETE", "\"$localbaseUrl$plantlistdeletePath\"")
        // AWS 실제 빌드
        buildConfigField("String", "API_PLANT_DETECT", "\"$detectPath\"")   // 식물 위치
//        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$baseUrl$recognizePath\"")      // 식물 이름
//        buildConfigField("String", "API_PLANT_REGISTER", "\"$baseUrl$registerPath\"")        // 식물 저장

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

    viewBinding {
        enable = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
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