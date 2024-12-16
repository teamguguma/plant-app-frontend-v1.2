package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // SharedPreferences를 통해 UUID 또는 사용자 데이터 확인
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val isUserExisting = prefs.contains("uuid") // UUID가 저장되어 있으면 기존 유저

        // 3초 딜레이 후 유저 상태에 따라 화면 전환
        Handler(Looper.getMainLooper()).postDelayed({
            if (isUserExisting) {
                // 기존 유저 -> 메인 화면으로 이동
                startActivity(Intent(this@IntroActivity, MainActivity::class.java))
            } else {
                // 신규 유저 -> 닉네임 입력 화면으로 이동
                startActivity(Intent(this@IntroActivity, NewUserActivity::class.java))
            }
            finish()
        }, 3000)
    }
}