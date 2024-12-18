package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class IntroActivity : AppCompatActivity() {

    private val client = OkHttpClient() // OkHttp 클라이언트 생성

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // SharedPreferences를 통해 UUID 확인
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null) // UUID 가져오기

        if (userUuid != null) {
            // UUID가 있으면 백엔드에 유저 확인 요청
            readUser(userUuid)
        } else {
            // UUID가 없으면 신규 유저 화면으로 이동
            moveToCreateUserActivity()
        }
    }

    /**
     * 기존 유저 확인 함수: 백엔드에 userUuid를 보내서 확인
     */
    private fun readUser(userUuid: String) {
        val url = BuildConfig.API_USER_READ // 서버 URL
        // URL에 쿼리 파라미터 추가
        val requestUrl = "$url?userUuid=$userUuid"
        // Request 객체 생성
        val request = Request.Builder()
            .url(requestUrl)
            .post("".toRequestBody(null)) // 빈 Body
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("IntroActivity", "Network Error", e)
                    moveToCreateUserActivity()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val responseJson = JSONObject(responseBody ?: "{}")

                        // 응답에서 'id' 확인
                        if (responseJson.has("id")) {
                            Log.d("IntroActivity", "Existing User ID: ${responseJson.getString("id")}")
                            moveToMainActivity()
                        } else {
                            Log.d("IntroActivity", "User does not exist")
                            moveToCreateUserActivity()
                        }
                    } else {
                        Log.e("IntroActivity", "Error: ${response.code} - ${response.message}")
                        moveToCreateUserActivity()
                    }
                }
            }
        })
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