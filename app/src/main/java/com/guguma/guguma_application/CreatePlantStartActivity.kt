package com.guguma.guguma_application

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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

class CreatePlantStartActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE = 1001 // 권한 코드 정의

    // 이미지 선택 결과를 처리할 pickImageLauncher 정의
    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { imageUri ->
                    // 갤러리에서 선택된 이미지 URI를 CreatePlantNameActivity로 전달
                    goToCreatePlantNameActivity(imageUri)
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

    // CreatePlantNameActivity로 이동
    private fun goToCreatePlantNameActivity(imageUri: Uri) {
        val intent = Intent(this, CreatePlantNameActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString()) // 선택한 이미지 URI 전달
        }
        startActivity(intent)
    }
}