package com.guguma.guguma_application

import android.app.DatePickerDialog
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
    private lateinit var addPlantButton: Button
    private var imageUri: Uri? = null
    private lateinit var lastWaterTextView: TextView
    private lateinit var lastWaterBtn: ImageButton
    private lateinit var waterIntervalEditText: EditText
    private lateinit var plantSearchView: EditText
    private lateinit var plantNicknameEditText: EditText

    private val recognizeUrl = BuildConfig.API_PLANT_RECOGNIZE
    private val registerUrl = BuildConfig.API_PLANT_REGISTER

    private var uploadedImageUrl: String? = null // 서버에서 반환된 이미지 URL 저장

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
        plantNicknameEditText = findViewById(R.id.plantNicknameEditText)

        imageView.clipToOutline = true

        // Intent 데이터 처리
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

        lastWaterBtn.setOnClickListener {
            showDatePickerDialog()
        }

        addPlantButton.setOnClickListener {
            val waterInterval = waterIntervalEditText.text.toString()
            if (waterInterval.isEmpty()) {
                Toast.makeText(this, "물 주기를 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                registerPlant()
            }
        }
    }

    private fun showDatePickerDialog() {
        // 현재 날짜
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
                lastWaterTextView.text = normalizeDateFormat(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun uploadImage(imageUri: Uri) {
        val TAG = "AddPlantActivity"
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)

        if (inputStream == null) {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageData = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "uploaded_image.jpg", imageData.toRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(recognizeUrl)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Image upload failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            // 서버에서 name 필드만 추출
                            val jsonResponse = JSONObject(responseBody)
                            val plantName = jsonResponse.optString("name", "이름 정보 없음")
                            val koreanOnly = plantName.replace("[^가-힣]".toRegex(), "")
                            plantSearchView.setText(koreanOnly) // 식물 이름을 UI에 설정
                            uploadedImageUrl = jsonResponse.optString("imageUrl", null)
                            if (!uploadedImageUrl.isNullOrEmpty()) {
                                Log.d(TAG, "Uploaded image URL: $uploadedImageUrl")
                            } else {
                                Toast.makeText(this@AddPlantActivity, "이미지 URL이 없습니다.", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "JSON Parsing error: ${e.message}")
                            Toast.makeText(this@AddPlantActivity, "식물 이름 추출 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@AddPlantActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun registerPlant() {
        if (uploadedImageUrl.isNullOrEmpty()) {
            Toast.makeText(this, "이미지가 업로드되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val plantName = plantSearchView.text.toString()
        val plantNickname = plantNicknameEditText.text.toString()
        val waterInterval = waterIntervalEditText.text.toString()
        val lastWateredDate = lastWaterTextView.text.toString()

        if (plantName.isEmpty() || waterInterval.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonBody = """
        {
            "name": "$plantName",
            "nickname": "$plantNickname",
            "waterInterval": $waterInterval,
            "lastWateredDate": "$lastWateredDate",
            "imageUrl": "$uploadedImageUrl",
            "user": { "id": 1 }
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(registerUrl)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AddPlantActivity, "등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddPlantActivity, "식물 등록 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddPlantActivity, "등록 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun normalizeDateFormat(date: String): String {
        return try {
            val parts = date.split("-")
            val year = parts[0].padStart(4, '0')
            val month = parts[1].padStart(2, '0')
            val day = parts[2].padStart(2, '0')
            "$year-$month-$day"
        } catch (e: Exception) {
            date
        }
    }
}