package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // SharedPreferences를 통해 UUID 확인
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null) // UUID 가져오기

        if (userUuid != null) {
            // UUID가 있으면 메인으로로 이동
            moveToMainActivity()
        } else {
            // UUID가 없으면 생성하고 저장
            createAndSaveUserUuid()
        }
    }

    private fun createAndSaveUserUuid() {
        val newUuid = UUID.randomUUID().toString() // 새로운 UUID 생성
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().putString("userUuid", newUuid).apply()
        moveToCreateUserActivity()
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToCreateUserActivity() {
        val intent = Intent(this, CreateUserActivity::class.java)
        startActivity(intent)
        finish()
    }
}