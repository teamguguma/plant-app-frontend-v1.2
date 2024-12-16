package com.guguma.guguma_application

import NicknameActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NewUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newuser)

        // 시작하기 버튼 찾기
        val buttonStart: Button = findViewById(R.id.buttonStart)

        // 버튼 클릭 시 NicknameActivity로 이동
        buttonStart.setOnClickListener {
            val intent = Intent(this, NicknameActivity::class.java)
            startActivity(intent)
        }
    }
}