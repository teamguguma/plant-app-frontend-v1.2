package com.guguma.guguma_application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ConfirmPlantActivity : AppCompatActivity() {

    private lateinit var plantImageView: ImageView
    private lateinit var confirmMessage: TextView
    private lateinit var confirmRegisterButton: Button
    private lateinit var editPlantNameButton: Button
    private lateinit var retakePhotoButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_plant)

        // 뷰 초기화
        plantImageView = findViewById(R.id.plantImageView)
        confirmMessage = findViewById(R.id.confirmMessage)
        confirmRegisterButton = findViewById(R.id.confirmRegisterButton)
        editPlantNameButton = findViewById(R.id.editPlantNameButton)
        retakePhotoButton = findViewById(R.id.retakePhotoButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Intent에서 데이터 가져오기
        val imageUriString = intent.getStringExtra("imageUri")
        val plantName = intent.getStringExtra("plantName")

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            Glide.with(this).load(imageUri).into(plantImageView)
        }

        confirmMessage.text = plantName ?: "인식된 이름이 없습니다."

        // 버튼 클릭 이벤트 처리
        confirmRegisterButton.setOnClickListener {
            registerPlant(plantName)
        }

        editPlantNameButton.setOnClickListener {
            showEditNameDialog(confirmMessage.text.toString())
        }

        retakePhotoButton.setOnClickListener {
            retakePhoto()
        }

        cancelButton.setOnClickListener {
            finish() // 화면 종료
        }
    }

    private fun registerPlant(plantName: String?) {
        // 등록 로직 작성
        val intent = Intent(this, AddPlantActivity::class.java).apply {
            putExtra("plantName", plantName)
        }
        startActivity(intent)
        finish()
    }

    private fun showEditNameDialog(currentName: String?) {
        // 다이얼로그 빌더 생성
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val plantNameEditText = dialogView.findViewById<EditText>(R.id.plantNameEditText)

        // 기존 이름을 EditText에 설정
        plantNameEditText.setText(currentName)

        // 다이얼로그 생성
        AlertDialog.Builder(this)
            .setTitle("식물 이름 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { dialog, _ ->
                val newName = plantNameEditText.text.toString()
                confirmMessage.text = newName  // 화면에 새로운 이름 반영
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun retakePhoto() {
        // 다시 촬영하도록 Camera 액티비티로 돌아가기
        val cameraIntent = Intent(this, Camera::class.java)
        startActivity(cameraIntent)
        finish()
    }
}