package com.guguma.guguma_application

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class CreatePlantNameActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var plantNameEditText: EditText
    private lateinit var retryPhotoButton: Button
    private lateinit var registerPlantButton: Button
    private lateinit var loadingTextView: TextView // 로딩 상태를 표시할 TextView

    private val okHttpClient = OkHttpClient()
    private val recognizeUrl = "API_PLANT_RECOGNIZE" // 식물 인식 API URL
    private var imageUri: Uri? = null
    private var imagePath: String? = null // 파일 경로 저장

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                loadImage(it)
                uploadImage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plant_name)

        val imageUrl = intent.getStringExtra("imageUrl")

        if (!imageUrl.isNullOrEmpty()) {
            // Glide로 이미지 로드
            Glide.with(this)
                .load(imageUrl)
                .into(findViewById(R.id.plantImageView))
        } else {
            Toast.makeText(this, "이미지 URL을 받지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // Glide를 사용하여 이미지 표시
    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .apply(RequestOptions.centerCropTransform())
            .into(imageView)
    }

    private fun uploadImage(imageUri: Uri) {
        // 로딩 상태 표시
        loadingTextView.text = "이미지 업로드 중입니다..."
        loadingTextView.visibility = TextView.VISIBLE
        plantNameEditText.setText("") // 이전 결과 초기화

        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageData = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "uploaded_image.jpg",
                imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(recognizeUrl)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingTextView.visibility = TextView.GONE // 로딩 상태 숨김
                    Toast.makeText(this@CreatePlantNameActivity, "이미지 업로드 실패", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val plantName = JSONObject(it).optString("name", "알 수 없음")
                    runOnUiThread {
                        loadingTextView.visibility = TextView.GONE // 로딩 상태 숨김
                        plantNameEditText.setText(plantName)
                    }
                }
            }
        })
    }

    private fun goToNicknameActivity() {
        val plantName = plantNameEditText.text.toString()
        if (plantName.isEmpty()) {
            Toast.makeText(this, "식물 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CreatePlantNicknameActivity::class.java).apply {
            val imageUrl = intent.getStringExtra("imageUrl")
            putExtra("plantName", plantName)
            putExtra("imagePath", imageUrl) // 이미지 파일 경로 전달
        }
        startActivity(intent)
    }
}