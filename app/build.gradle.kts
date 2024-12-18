import java.util.Properties



plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    //id("kotlin-kapt")

}

android {
    namespace = "com.guguma.guguma_application"
    compileSdk = 34

    lint {
        checkDependencies = true // 종속성의 lint 검사 활성화
        abortOnError = false // 오류가 있어도 빌드 중단하지 않음
        warningsAsErrors = false // 경고를 오류로 처리하지 않음
    }

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
        val detectPath = localProperties.getProperty("api.plant.detect")

        buildConfigField("String", "API_BASE_URL", "\"$baseUrl\"")

        buildConfigField("String", "API_PLANT_RECOGNIZE", "\"$localbaseUrl/plants/recognize\"")//이름검색및이름저장
        buildConfigField("String", "API_PLANT_DETECT", "\"$detectPath\"")

        buildConfigField("String", "API_USER_CREATE", "\"$localbaseUrl/users/create\"")
        buildConfigField("String", "API_USER_READ", "\"$localbaseUrl/users/read\"")
        buildConfigField("String", "API_USER_DELETE", "\"$localbaseUrl/users/delete\"")

        buildConfigField("String", "API_PLANT_LIST", "\"$localbaseUrl\"")
        buildConfigField("String", "API_PLANT_DELETE", "\"$localbaseUrl\"")
        buildConfigField("String", "API_PLANT_CREATE", "\"$localbaseUrl/plants/create\"")

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
val camerax_version = "1.3.0"
dependencies {
    // CameraX (최신 BOM 사용)
    implementation ("androidx.camera:camera-core:$camerax_version")
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    implementation ("androidx.camera:camera-view:$camerax_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
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
    // Guava 추가 (ListenableFuture 지원)
    implementation ("com.google.guava:guava:31.1-android")

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // GSON (버전 수정)
    implementation ("com.google.code.gson:gson:2.10.1")

    // OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Firebase
    implementation ("com.google.firebase:firebase-analytics:21.4.0")

    // AndroidX
    implementation ("androidx.core:core-ktx:1.12.0") // 최신 버전 사용
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.preference:preference:1.2.0")

    // 테스트 의존성
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
}

//room 모듈을 불러오기 위해 kapt 설치를 진행했으나 jdk버전이 충돌한다는 이유로 빌드조차 되지 않음 , kapt->ksp로 변경 후 충돌 없이 잘 돌아감