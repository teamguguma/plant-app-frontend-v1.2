package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)  // setTheme 다음에 호출
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    //홈 화면 처리
                    replaceFragment(HomeFragment())
                    //item.setIcon(R.drawable.) 아이콘 바꾸기 효과 주려고 도전 중
                    true
                }
                R.id.nav_camera -> {
                    //카메라 화면 처리 (카메라 프래그먼트가 아닌 카메라 액티비티로 이동)
                    val intent = Intent(this, Camera::class.java)  // CameraActivity 실행
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment()) //기본화면 = 홈으로 설정
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit()
    }
}