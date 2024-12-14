package com.guguma.guguma_application

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPlantActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var addPlantButton: Button // 등록하기 버튼
    private var imageUri: Uri? = null
    private lateinit var lastWaterTextView: TextView // 마지막으로 물 준 날짜 TextView
    private lateinit var lastWaterBtn: ImageButton // 마지막으로 물 준 날짜 변경 버튼
    private lateinit var waterIntervalEditText: EditText // 물 주기 (며칠에 한 번)
    private lateinit var plantSearchView: EditText // 식물 이름 EditText
    private lateinit var plantNicknameEditText: EditText  // 식물 별명
    val recognizeUrl = BuildConfig.API_PLANT_RECOGNIZE
    val registerUrl = BuildConfig.API_PLANT_REGISTER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        // View 초기화
        plantSearchView = findViewById(R.id.plantSearchView)
        addPlantButton = findViewById(R.id.addPlantBtn)
        imageView = findViewById(R.id.userPlantPic)
        lastWaterTextView = findViewById(R.id.LastWater)
        lastWaterBtn = findViewById(R.id.LastWaterBtn)
        waterIntervalEditText = findViewById(R.id.WaterInterval)
        plantNicknameEditText = findViewById(R.id.plantNicknameEditText) // 별명 EditText 초기화

        imageView.clipToOutline = true

        // Intent에서 이미지 URI 및 식물 이름 가져오기
        val uriString = intent.getStringExtra("imageUri")
        val plantName = intent.getStringExtra("plantName")
        val plantNickname = intent.getStringExtra("plantNickname")

        if (!uriString.isNullOrEmpty()) {
            imageUri = Uri.parse(uriString)
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)
            uploadImage(imageUri!!)
        }

        if (!plantName.isNullOrEmpty()) {
            plantSearchView.setText(plantName)
        }

        // 현재 날짜 설정
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        lastWaterTextView.text = currentDate

        // 날짜 변경 버튼 클릭 이벤트
        lastWaterBtn.setOnClickListener {
            showDatePickerDialog()
        }

        // 등록하기 버튼 클릭 이벤트
        addPlantButton.setOnClickListener {

            val waterInterval = waterIntervalEditText.text.toString()
            if (waterInterval.isNotEmpty()) {
                Toast.makeText(this, "물 주기: $waterInterval 일에 한 번", Toast.LENGTH_SHORT).show()
                // 물 주기 값과 함께 서버로 데이터를 전송하거나 저장
            } else {
                Toast.makeText(this, "물 주기를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            registerPlant()
        }
    }

    private fun showDatePickerDialog() {
        // 현재 날짜
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog 생성
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜 설정
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                lastWaterTextView.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun uploadImage(imageUri: Uri) {
        val TAG = "AddPlantActivity"

        // Uri에서 InputStream 가져오기
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // InputStream을 Bitmap으로 변환
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Bitmap 압축
        val stream = ByteArrayOutputStream()
        var quality = 100
        do {
            stream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            quality -= 10
        } while (stream.toByteArray().size > 1024 * 1024 && quality > 0) // 1MB 이하로 압축

        // 이미지 전송 준비
        val imageData = stream.toByteArray()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "compressed_image.jpg", imageData.toRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_RECOGNIZE)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Image upload failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "이미지 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val plantName = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && plantName != null) {
                        plantSearchView.setText(plantName)
                    } else {
                        Toast.makeText(this@AddPlantActivity, "식물 인식 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }
    private fun normalizeDateFormat(date: String): String {
        // 입력된 날짜가 올바른 형식인지 확인하고 수정
        return try {
            val parts = date.split("-")
            if (parts.size == 3) {
                val year = parts[0].padStart(4, '0')
                val month = parts[1].padStart(2, '0')
                val day = parts[2].padStart(2, '0')
                "$year-$month-$day" // yyyy-MM-dd 형식 반환
            } else {
                date // 원본 반환 (비정상 입력)
            }
        } catch (e: Exception) {
            date // 예외 발생 시 원본 반환
        }
    }
    private fun registerPlant() {
        val TAG = "AddPlantActivity"

        // 입력값 가져오기
        val plantName = plantSearchView.text.toString()
        val plantNickname = plantNicknameEditText.text.toString()
        val waterInterval = waterIntervalEditText.text.toString()
        var lastWateredDate = lastWaterTextView.text.toString()

        // 날짜 형식 자동 정리
        lastWateredDate = normalizeDateFormat(lastWateredDate)

        if (plantName.isEmpty() || waterInterval.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // JSON 요청 바디 생성
        val jsonBody = """
        {
            "name": "$plantName",
            "nickname": "$plantNickname",
            "waterInterval": $waterInterval,
            "lastWateredDate": "$lastWateredDate",
            "imageUrl": "http://example.com/image.jpg",
            "characteristics": "식물간단설명테스트임시",
            "user": { "id": 1 } 
        }
    """.trimIndent()
        // JSON 디버그 로그 출력
        Log.d(TAG, "Request JSON: $jsonBody")

        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        // HTTP 요청 생성
        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_REGISTER) // 백엔드 API 엔드포인트
            .post(requestBody)
            .build()

        // HTTP 요청 실행
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to register plant: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "식물 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddPlantActivity, "식물 등록 성공", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()
                        resultIntent.putExtra("newPlantName", plantName)
                        resultIntent.putExtra("newPlantNickname", plantNickname)
                        resultIntent.putExtra("newPlantImageUrl", "http://example.com/image.jpg") // 예제 이미지 URL
                        setResult(Activity.RESULT_OK, resultIntent) // 결과 설정
                        finish() // 등록 후 화면 종료
                    } else {
                        Toast.makeText(this@AddPlantActivity, "식물 등록 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }

}