import java.util.*



plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    //id("kotlin-kapt")

}

android {
    namespace = "com.guguma.guguma_application"
    compileSdk = 34

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }
    val API_PLANT_DETECT = localProperties.getProperty("api.key.plant.detect") ?: ""
    val API_PLANT_RECOGNIZE = localProperties.getProperty("api.key.plant.recognize") ?: ""

    defaultConfig {
        applicationId = "com.guguma.guguma_application"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_PLANT_DETECT", "\"$API_PLANT_DETECT\"")
        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$API_PLANT_RECOGNIZE\"")
        resValue("string", "API_PLANT_DETECT", API_PLANT_DETECT)
        resValue("string", "API_PLANT_RECOGNIZE", API_PLANT_RECOGNIZE)


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

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")//room모델
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") //view 모델
    implementation("com.google.code.gson:gson:2.11.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
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

//room 모듈을 불러오기 위해 kapt 설치를 진행했으나 jdk버전이 충돌한다는 이유로 빌드조차 되지 않음 , kapt->ksp로 변경 후 충돌 없이 잘 돌아감