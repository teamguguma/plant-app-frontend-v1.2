package com.guguma.guguma_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent

class intro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@intro, MainActivity::class.java) //:: 이부분,,  인트로 화면 3초 뒤에 로그인 화면으로 연결하겠다는 것
            startActivity(intent)
            finish()
        }, 3000) //= 3초 뒤에 메인액티비티로~
    }
}