package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Intent에서 userUuid 가져오기
        var userUuid = intent.getStringExtra("userUuid")
        if (userUuid == null) {
            // SharedPreferences에서 userUuid 가져오기
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            userUuid = prefs.getString("userUuid", null)
        }

//        if (userUuid == null) {
//            Log.e("MainActivity", "userUuid is null!")
//            // 오류 처리: 닉네임 설정 화면으로 이동
//            startActivity(Intent(this, CreateUserUesrnameActivity::class.java))
//            finish()
//            return
//        }

        Log.d("MainActivity", "userUuid: $userUuid")

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // 네비게이션 아이템 처리
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    //item.setIcon(R.drawable.) 아이콘 바꾸기 효과 주려고 도전 중
                    true
                }
                    true
                }
                R.id.nav_camera -> {
                    startActivity(Intent(this, CameraActivity::class.java))
                    true
                    //카메라 화면 처리 (카메라 프래그먼트가 아닌 카메라 액티비티로 이동)
                    val intent = Intent(this, Camera::class.java)  // CameraActivity 실행
                    startActivity(intent)
                    true
                }
                R.id.nav_info -> {
                    replaceFragment(InfoFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment()) // 기본 화면
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit()
    }
}