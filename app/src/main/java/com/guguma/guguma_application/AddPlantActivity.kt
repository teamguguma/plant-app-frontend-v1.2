package com.guguma.guguma_application

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

// 식물 상세 페이지
class AddPlantActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var plantNameTextView: TextView
    private lateinit var plantStatusTextView: TextView
    private lateinit var plantRemedyTextView: TextView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        // View 초기화
        imageView = findViewById(R.id.userPlantPic)
        plantNameTextView = findViewById(R.id.plantSearchView)
        plantStatusTextView = findViewById(R.id.plantStatusText)
        plantRemedyTextView = findViewById(R.id.plantNicknameEditText)
        //addPlantButton = findViewById(R.id.addPlantButton)

        // 전달받은 ID 가져오기
        val plantId = intent.getIntExtra("plantId", -1)
        if (plantId != -1) {
            fetchPlantDetails(plantId)
        } else {
            Toast.makeText(this, "유효하지 않은 식물 ID입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlantDetails(plantId: Int) {
        val url = "${BuildConfig.BASE_URL}/plants/$plantId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AddPlantActivity", "Failed to fetch plant details: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "식물 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = JSONObject(responseBody.string())
                        val name = json.optString("name", "알 수 없음")
                        val status = json.optString("status", "알 수 없음")
                        val remedy = json.optString("remedy", "알 수 없음")
                        val imageUrl = json.optString("imageUrl", "")

                        // UI 업데이트
                        runOnUiThread {
                            plantNameTextView.text = name
                            plantStatusTextView.text = status
                            plantRemedyTextView.text = remedy
                            if (imageUrl.isNotEmpty()) {
                                Glide.with(this@AddPlantActivity)
                                    .load(imageUrl)
                                    .apply(RequestOptions.centerCropTransform())
                                    .into(imageView)
                            }
                        }
                    }
                } else {
                    Log.e("AddPlantActivity", "Error: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@AddPlantActivity, "식물 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}