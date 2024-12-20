package com.guguma.guguma_application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.guguma.guguma_application.CameraActivity
class CreatePlantStartActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 1001 // 권한 코드 정의
    private val client = OkHttpClient() // OkHttpClient 정의

    // 이미지 선택 결과를 처리할 pickImageLauncher 정의
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { imageUri ->
                    // 갤러리에서 선택된 이미지 URI를 서버로 업로드
                    uploadImageToServer(imageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plant_start)

        // 갤러리 버튼 설정
        val bringGallery: Button = findViewById(R.id.bringGalleryBtn)
        bringGallery.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        val picAddButton: Button = findViewById(R.id.picAddBtn)

        // 버튼 클릭 리스너 설정
        picAddButton.setOnClickListener {
            // 사진 촬영을 위한 액티비티로 이동
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 뒤로가기 버튼 설정
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // 현재 화면 종료
        }
    }

    // 갤러리 접근 권한 체크 및 열기
    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용된 경우
                navigatePhotos()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // 권한 거부 후 추가 설명이 필요한 경우
                showPermissionContextPopup(permission)
            }
            else -> {
                // 권한 요청 실행
                requestPermissions(arrayOf(permission), STORAGE_PERMISSION_CODE)
            }
        }
    }

    // 권한 요청 교육용 팝업
    private fun showPermissionContextPopup(permission: String) {
        AlertDialog.Builder(this)
            .setTitle("갤러리 권한 필요")
            .setMessage("갤러리에서 사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("권한 허용") { _, _ ->
                requestPermissions(arrayOf(permission), STORAGE_PERMISSION_CODE)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "갤러리 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "저장소 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                navigatePhotos()
            } else {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리 열기
    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // 이미지 서버 업로드 함수
    private fun uploadImageToServer(imageUri: Uri) {
        val compressedBytes = compressImageToByteArray(imageUri, 1024 * 1024) // 1MB 이하로 압축

        if (compressedBytes != null) {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(BuildConfig.API_CAMERA_STATUS) // 서버 업로드 URL
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CreatePlantStartActivity,
                            "이미지 업로드 실패: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val fileUrl = response.body?.string().orEmpty()
                        runOnUiThread {
                            val intent =
                                Intent(this@CreatePlantStartActivity, DetailPlantActivity::class.java).apply {
                                    putExtra("imageUrl", fileUrl) // 업로드된 이미지 URL 전달
                                }
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@CreatePlantStartActivity,
                                "서버 오류: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        } else {
            Toast.makeText(this, "이미지 압축에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 이미지 압축 함수
    private fun compressImageToByteArray(uri: Uri, maxSize: Int): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            var quality = 90 // 초기 압축 품질
            var byteArray: ByteArray

            do {
                outputStream.reset()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 10 // 압축 품질을 10씩 감소
            } while (byteArray.size > maxSize && quality > 10)

            outputStream.close()
            byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}