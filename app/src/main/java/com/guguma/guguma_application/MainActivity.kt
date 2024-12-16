package com.guguma.guguma_application

import NewNicknameActivity
import UUIDManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guguma.guguma_application.network.ApiClient
import com.guguma.guguma_application.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val uuid = UUIDManager.getOrCreateUUID(this)

        // 백엔드에 로그인 요청
        ApiClient.userService.loginOrCreateUser(uuid, null)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user?.username.isNullOrEmpty()) {
                            goToNicknameActivity(uuid)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
//                                "안녕하세요, ${user?.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "로그인 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })


        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    //홈 화면 처리
                    replaceFragment(HomeFragment())
                    //item.setIcon(R.drawable.) 아이콘 바꾸기 효과 주려고 도전 중
                    true
                }

                R.id.nav_doctor -> {
                    //식물 진단 화면 처리
                    replaceFragment(DoctorFragment())
                    true
                }

                R.id.nav_camera -> {
                    //카메라 화면 처리 (카메라 프래그먼트가 아닌 카메라 액티비티로 이동)
                    val intent = Intent(this, Camera::class.java)  // CameraActivity 실행
                    startActivity(intent)
                    true
                }

                R.id.nav_myPlant -> {
                    //나의 정원 화면 처리
                    replaceFragment(MyPlantFragment())
                    true
                }

                R.id.nav_info -> {
                    //설정 화면 처리
                    replaceFragment(InfoFragment())
                    true
                }

                else -> false
            }
        }
        replaceFragment(HomeFragment()) //기본화면 = 홈으로 설정
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit()
    }

    private fun goToNicknameActivity(uuid: String) {
        val intent = Intent(this, NicknameActivity::class.java)
        intent.putExtra("uuid", uuid)
        startActivity(intent)
    }
}