package com.guguma.guguma_application

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.guguma.guguma_application.dto.PlantDto
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class PlantAdapter(

    private val context: Context,
    private val plantList: MutableList<PlantDto>,
    private val onDelete: (Long) -> Unit, // 삭제 버튼 클릭 시 호출할 콜백
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    // 선택 모드 상태
    var isSelectable = false
        private set // 외부에서 수정 불가

    // 선택 모드 종료 메서드
    fun exitSelectionMode() {
        if (isSelectable) {
            isSelectable = false
            plantList.forEach { it.isSelected = false }
            notifyDataSetChanged() // UI 업데이트
        }
    }

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.plantcheckBox)
        val deleteButton: Button = itemView.findViewById(R.id.delButton)
        val plantNameTextView: TextView = itemView.findViewById(R.id.plantNameTextView)
        val plantNicknameTextView: TextView = itemView.findViewById(R.id.plantNicknameTextView)
        val plantImageView: ImageView = itemView.findViewById(R.id.plantImageView)

        fun bind(plant: PlantDto) {
            plantNameTextView.text = plant.name
            plantNicknameTextView.text = plant.nickname
            Glide.with(context).load(plant.imageUrl).into(plantImageView)

            // 체크박스 표시 여부
            checkBox.visibility = if (isSelectable) View.VISIBLE else View.GONE
            checkBox.isChecked = plant.isSelected

            // 삭제 버튼 활성화 여부
            deleteButton.isEnabled = !isSelectable
            deleteButton.alpha = if (isSelectable) 0.5f else 1.0f // 버튼 비활성화 시 시각적 효과
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position]
        holder.bind(plant)

        // 길게 누르기 이벤트: 선택 모드 활성화
        holder.itemView.setOnLongClickListener {
            if (!isSelectable) {
                isSelectable = true
                notifyDataSetChanged() // 선택 모드 활성화
            }
            true
        }

        // 클릭 이벤트: 상세 화면 이동 또는 체크박스 상태 변경
        holder.itemView.setOnClickListener {
            if (isSelectable) {
                plant.isSelected = !plant.isSelected
                holder.checkBox.isChecked = plant.isSelected
            } else {
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("plantName", plant.name)
                    putExtra("plantNickname", plant.nickname)
                    putExtra("plantImageUrl", plant.imageUrl)
                }
                context.startActivity(intent)
            }
        }


        holder.itemView.findViewById<Button>(R.id.delButton).setOnClickListener {
            // 모달 창 띄우기
            AlertDialog.Builder(context)
                .setTitle("삭제 확인")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    // 확인 버튼 클릭 시 삭제 요청
                    deletePlantFromServer(plant.id) { success ->
                        if (success) {
                            // 성공 시 RecyclerView에서 삭제
                            removeItem(position)
                            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            // 실패 시 사용자에게 알림
                            Toast.makeText(context, "삭제 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("취소") { dialog, _ ->
                    // 취소 버튼 클릭 시 다이얼로그 닫기
                    dialog.dismiss()
                }
                .show()
        }

    }



    private fun deletePlantFromServer(plantId: Long, callback: (Boolean) -> Unit) {
        val BASE_URL = BuildConfig.API_PLANT_DELETE ?: throw IllegalArgumentException("API URL이 null입니다.")
        val deleteUrl = "$BASE_URL/$plantId" // 서버의 DELETE API URL

        // OkHttpClient에 HttpLoggingInterceptor 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val request = Request.Builder()
            .url(deleteUrl)
            .delete() // HTTP DELETE 요청
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    Log.e("DeleteDebug", "Request failed: ${e.message}")
                    Toast.makeText(context, "삭제 바나나: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                callback(false) // 실패 시 false 반환
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string() // 백그라운드 스레드에서 처리
                (context as Activity).runOnUiThread {
                    Log.d("DeleteDebug", "Response Code: ${response.code}")
                    Log.d("DeleteDebug", "Response Body: $responseBodyString")
                    if (response.isSuccessful) {
                        Log.d("DeleteDebug", "Delete successful")
                        callback(true) // 성공 여부 반환
                    } else {
                        Log.e("DeleteDebug", "Delete failed: ${response.message}")
                        Toast.makeText(context, "삭제 사과", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
            }
        })
    }

    // getItemCount: 아이템 총 개수 반환
    override fun getItemCount(): Int = plantList.size

    // 특정 위치의 데이터를 가져오는 getItem 메서드
    fun getItem(position: Int): PlantDto {
        return plantList[position]
    }

    // 데이터 업데이트 메서드
    fun updateData(newPlantList: List<PlantDto>) {
        plantList.clear()
        plantList.addAll(newPlantList)
        notifyDataSetChanged() // 데이터 변경 후 UI 갱신
    }

    // 아이템 삭제 메서드 (Delete)
    fun removeItem(position: Int) {
        plantList.removeAt(position)
        notifyItemRemoved(position)
    }

    // 아이템 추가 메서드 (Create)
    fun addItem(plant: PlantDto) {
        plantList.add(plant)
        notifyItemInserted(plantList.size - 1)
    }

    // 아이템 수정 메서드 (Update)
    fun updateItem(position: Int, updatedPlant: PlantDto) {
        plantList[position] = updatedPlant
        notifyItemChanged(position)
    }
}