package com.example.guguma

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.guguma.guguma_application.BuildConfig
import com.guguma.guguma_application.MainActivity
import com.guguma.guguma_application.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class CreateUserUsernameActivity : AppCompatActivity() {

    private val client = OkHttpClient() // OkHttp 클라이언트 생성

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user_username)

        val usernameInput: EditText = findViewById(R.id.editTextUsername)
        val submitButton: Button = findViewById(R.id.buttonSubmitUsername)

        submitButton.setOnClickListener {
            val userusername = usernameInput.text.toString().trim()

            when {
                userusername.isEmpty() -> {
                    Toast.makeText(this, "닉네임을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }
                !userusername.matches(Regex("^[가-힣]{1,10}$")) -> {
                    Toast.makeText(this, "닉네임은 한글 1~10자만 가능합니다.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val userUuid = UUID.randomUUID().toString()
                    saveUserUuidLocally(userUuid)
                    createUser(userUuid, userusername)
                }
            }
        }
    }

    // 로컬에 UUID 저장
    private fun saveUserUuidLocally(userUuid: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().putString("userUuid", userUuid).apply()

        // 저장된 데이터 확인 로그
        val savedUuid = prefs.getString("userUuid", null)
        Log.d("SaveUserUuid", "Saved UUID: $savedUuid")
    }

    // 서버에 UUID와 닉네임 저장 (OkHttp 사용)
    private fun createUser(userUuid: String, userusername: String) {
        val url = BuildConfig.API_USER_CREATE // 서버 URL
        Log.d("API_URL", "User Create API: $url")
        // JSON 데이터 생성
        val jsonObject = JSONObject()
        jsonObject.put("userUuid", userUuid)
        jsonObject.put("username", userusername)

        // JSON 형태의 RequestBody 생성
        val requestBody = RequestBody.create("application/json".toMediaType(), jsonObject.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json") // 헤더 설정
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("CreateUserUsername", "Network Error", e)
                    Toast.makeText(this@CreateUserUsernameActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateUserUsernameActivity, "닉네임이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                        moveToMainActivity()
                    } else {
                        Log.e("CreateUserusername", "Error: ${response.code} - ${response.message}")
                        Toast.makeText(this@CreateUserUsernameActivity, "서버 오류: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // 메인 액티비티로 이동
    private fun moveToMainActivity() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null)

        // 로드된 UUID 확인
        Log.d("MoveToMainActivity", "Loaded UUID: $userUuid")

        if (userUuid == null) {
            Toast.makeText(this, "사용자 UUID를 찾을 수 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userUuid", userUuid)
        startActivity(intent)
        finish()
    }
}