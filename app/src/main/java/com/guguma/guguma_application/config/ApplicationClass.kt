package com.guguma.guguma_application.config

import android.app.Application
import com.guguma.guguma_application.repository.PlantListRepository

class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()

        PlantListRepository.initialize(this)
    }
}

//이 클래스의 경우 앱이 실행될 때 단 한번 실행되도록 하기 위해 작성
//앱 실행과 동시에 Repository 초기화를 통해 데이터베이스가 없을 경우 새로 빌드하도록 함.
//이 클래스가 동작하도록 하기위해 Manifests에 아래와 같은 코드 추가
//<application
//        android:name=".config.ApplicationClass"
// ....
//</application>