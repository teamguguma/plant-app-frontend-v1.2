package com.guguma.guguma_application

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.appcompat.app.AlertDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull

// 식물 상세 페이지
class DetailPlantActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var plantNameTextView: TextView
    private lateinit var plantNameEditText: EditText
    private lateinit var plantRemedyTextView: TextView
    private lateinit var plantRemedyEditText: EditText
    private lateinit var nameEditButton: ImageButton
    private lateinit var remedyEditButton: ImageButton
    private lateinit var addButton: Button

    private val client = OkHttpClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)


        // View 초기화
        imageView = findViewById(R.id.userPlantPic)
        plantNameTextView = findViewById(R.id.plantSearchView)
        plantNameEditText = findViewById(R.id.plantSearchEditText)
        nameEditButton = findViewById(R.id.SearchEditButton)
        plantRemedyTextView = findViewById(R.id.plantRemedyText)
        plantRemedyEditText = findViewById(R.id.plantRemedyEditText)
        remedyEditButton = findViewById(R.id.RemedyEditButton)
        addButton = findViewById(R.id.addButton)


        // 전달받은 ID 가져오기
        val plantName = intent.getStringExtra("plantName") ?: "알 수 없음"
        val plantstatus = intent.getStringExtra("plantStatus") ?: "알 수 없음"
        val plantRemedy = intent.getStringExtra("plantRemedy") ?: "알 수 없음"
        val imageUrl = intent.getStringExtra("plantImageUrl") ?: ""

        plantNameTextView.text = plantName
        plantRemedyTextView.text = plantRemedy
        loadImage(imageUrl)

        nameEditButton.setOnClickListener {
            showEditDialog(plantNameTextView, "식물 종 이름을 입력하세요")
        }

        remedyEditButton.setOnClickListener {
            showEditDialog(plantRemedyTextView, "관리 방법을 입력하세요")
        }

        addButton.setOnClickListener{
            val nickname = findViewById<EditText>(R.id.plantNicknameEditText).text.toString()
            val checkDateInterval = findViewById<EditText>(R.id.dateEditText).text.toString().toIntOrNull() ?: 0
            sendInfoToServer(nickname, plantName, checkDateInterval, plantstatus, plantRemedy, imageUrl)
            finish()
        }

    }

    private fun loadImage(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this)
                .load(url)
                .apply(RequestOptions.centerCropTransform())
                .into(imageView)
        }
    }

    private fun showEditDialog(textView: TextView, dialogHint: String) {
        val editText = EditText(this).apply {
            hint = dialogHint  // 사용자가 어떤 정보를 입력해야 하는지 힌트를 제공
            setSelectAllOnFocus(true)
        }
        AlertDialog.Builder(this)
            .setTitle("정보 수정")
            .setView(editText)
            .setPositiveButton("확인") { dialog, _ ->
                textView.text = editText.text.toString()
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun sendInfoToServer(nickname: String, name: String, checkDateInterval: Int, status: String, remedy: String, imageUrl: String){
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userUuid = prefs.getString("userUuid", null)

        if (userUuid.isNullOrEmpty()) {
            Toast.makeText(this, "UUID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("userUuid", userUuid)
            put("nickname", nickname)
            put("name", name)
            put("checkDateInterval", checkDateInterval)
            put("status", status)
            put("remedy", remedy)
            put("imageUrl", imageUrl)
        }

        // 서버로 보낼 준비
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_UPLOAD) // 서버 업로드 URL
            .post(requestBody)
            .build()

        // 서버에 요청을 비동기로 보내고 응답 처리
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DetailPlantActivity, "데이터 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailPlantActivity, "데이터가 성공적으로 업데이트 되었습니다.", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK) // 성공 결과 설정
                        finish() // Activity 종료
                    } else {
                        Toast.makeText(this@DetailPlantActivity, "서버 오류: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }
}