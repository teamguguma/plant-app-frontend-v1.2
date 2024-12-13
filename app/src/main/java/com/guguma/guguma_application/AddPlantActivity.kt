package com.guguma.guguma_application

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.*
import android.widget.TextView
import android.widget.Toast
import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider // ViewModelProvider 임포트 추가

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.util.Log

class AddPlantActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var addPlantButton: Button // 등록하기 버튼
    private var imageUri: Uri? = null
    private lateinit var addDateTextView: TextView // 등록 일자 TextView
    private lateinit var lastWaterBtn: ImageButton
    private lateinit var lastWaterTextView: TextView
    private lateinit var dateBtn: ImageButton // 날짜 변경 버튼
    private lateinit var spinnerAddPlantWater: Spinner // Spinner 추가
    private lateinit var plantSearchView: EditText //식물 종

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        plantSearchView = findViewById(R.id.plantSearchView) // findViewById는 한 번만 실행
        addPlantButton = findViewById(R.id.addPlantBtn)
        imageView = findViewById(R.id.userPlantPic)

        val uriString = intent.getStringExtra("imageUri")
        val plantName = intent.getStringExtra("plantName")

        if (!uriString.isNullOrEmpty()) {
            imageUri = Uri.parse(uriString)
            Glide.with(this).load(imageUri).into(imageView)
        }

        if (!plantName.isNullOrEmpty()) {
            plantSearchView.setText(plantName)
        }
        
        addDateTextView = findViewById(R.id.AddDate) // 등록 일자 텍스트뷰
        dateBtn = findViewById(R.id.DateBtn) // 날짜 변경 버튼
        lastWaterTextView = findViewById(R.id.LastWater) // 마지막으로 물 준 날짜 일자 텍스트뷰
        lastWaterBtn = findViewById(R.id.LastWaterBtn) // 마지막으로 물 준 날짜 변경 버튼
        spinnerAddPlantWater = findViewById(R.id.spinner_addplantwater) // Spinner 연결
        imageView.clipToOutline = true

        // 이미지 URI를 전달받기
        if (!uriString.isNullOrEmpty()) {
            imageUri = Uri.parse(uriString) // String을 URI로 변환
            Glide.with(this)
                .load(imageUri) // 이미지 URI를 로드
                .apply(RequestOptions.circleCropTransform()) // 원형 이미지로 변환
                .into(imageView) // ImageView에 이미지 설정
            uploadImage(imageUri!!)
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        addDateTextView.text = currentDate
        lastWaterTextView.text = currentDate

        dateBtn.setOnClickListener {
            showDatePickerDialog()
        }

        lastWaterBtn.setOnClickListener {
            showDatePickerDialog()
        }

        addPlantButton.setOnClickListener {
            val plantsearchview = binding.etPlantListname.text.toString() //만드는 중...
        }

        val outCategoryList = listOf("월", "주", "일")
        spinnerAddPlantWater.adapter = CategorySpinnerAdapter(this, R.layout.item_spinner_addplant, outCategoryList)
        spinnerAddPlantWater.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = spinnerAddPlantWater.getItemAtPosition(position).toString()
                // 선택된 값에 대한 추가 작업
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 선택되지 않은 경우 처리
            }
        }
    }

    private fun showDatePickerDialog() {
        // 현재 날짜에서 년, 월, 일 추출
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // DatePickerDialog 생성
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜를 텍스트 형식으로 변환하여 TextView에 설정
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                addDateTextView.text = selectedDate
                lastWaterTextView.text = selectedDate
            },
            year, month, day
        )
        // DatePickerDialog 표시
        datePickerDialog.show()
    }

    private fun uploadImage(imageUri: Uri) {
        val TAG = "AddPlantActivity"

        // Uri를 통해 InputStream 가져오기
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // InputStream을 Bitmap으로 변환
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Bitmap 압축하여 ByteArrayOutputStream에 저장
        val stream = ByteArrayOutputStream()
        var quality = 100
        do {
            stream.reset() // 스트림을 리셋
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream) // Bitmap 압축
            quality -= 10 // 품질을 점진적으로 감소
        } while (stream.toByteArray().size > 1024 * 1024 && quality > 0) // 1MB 이하가 될 때까지 반복

        // 압축된 이미지를 ByteArray로 변환
        val imageData = stream.toByteArray()

        // OkHttpClient로 이미지 전송
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "compressed_image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageData))
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
                Log.d(TAG, "Image upload response received")
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

}

//이미지를 둥글게 표현하기
//imageView.clipToOutline = true