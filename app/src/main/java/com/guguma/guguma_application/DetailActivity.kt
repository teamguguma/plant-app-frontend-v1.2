package com.guguma.guguma_application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 데이터 수신
        val plantName = intent.getStringExtra("plantName")
        val plantNickname = intent.getStringExtra("plantNickname")
        val plantImageUrl = intent.getStringExtra("plantImageUrl")

        // UI에 데이터 바인딩
        val nameTextView = findViewById<TextView>(R.id.plantName_detail_post_textview)
        val nicknameTextView = findViewById<TextView>(R.id.plantNickname_detail_textview)
        val imageView = findViewById<ImageView>(R.id.detail_img_plant)

        nameTextView.text = plantName
        nicknameTextView.text = plantNickname
        Glide.with(this).load(plantImageUrl).into(imageView)


        val backButton = findViewById<ImageButton>(R.id.detail_back_imgbtn)
        backButton.setOnClickListener {
            finish() // 현재 Activity를 종료하고 이전 화면으로 돌아감
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }


}