package com.guguma.guguma_application

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.guguma.guguma_application.dto.PlantDto
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.IOException

class PlantAdapter(
    private val context: Context,
    private val plantList: MutableList<PlantDto>,
    private val onDelete: (Long) -> Unit // 삭제 버튼 클릭 시 호출할 콜백
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    // ViewHolder 클래스 정의
    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val deleteButton: Button = itemView.findViewById(R.id.manageButton)
        val plantNameTextView: TextView = itemView.findViewById(R.id.plantNameTextView)
        val plantNicknameTextView: TextView = itemView.findViewById(R.id.plantNicknameTextView)
        val plantImageView: ImageView = itemView.findViewById(R.id.plantImageView)

        fun bind(plant: PlantDto) {
            plantNameTextView.text = plant.name
            plantNicknameTextView.text = plant.nickname
            Glide.with(context).load(plant.imageUrl).into(plantImageView)
        }
    }

    // onCreateViewHolder: 레이아웃 인플레이션 및 ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_plant, parent, false)
        return PlantViewHolder(view)
    }

    // onBindViewHolder: ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position] // 현재 식물 데이터
        holder.bind(plant)

        // 아이템 클릭 이벤트 설정
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("plantName", plant.name)
                putExtra("plantNickname", plant.nickname)
                putExtra("plantImageUrl", plant.imageUrl)
            }
            context.startActivity(intent)
        }


        // 삭제 버튼 클릭 이벤트 설정
        holder.itemView.findViewById<Button>(R.id.manageButton).setOnClickListener {
            // 서버로 삭제 요청
            deletePlantFromServer(plant.id) { success ->
                if (success) {
                    // 성공 시 RecyclerView에서 삭제
                    removeItem(position)
                } else {
                    // 실패 시 사용자에게 알림
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // 서버에 삭제 요청 보내는 함수
    private fun deletePlantFromServer(plantId: Long, callback: (Boolean) -> Unit) {
        val deleteUrl = "http://your-server-url.com/api/plants/$plantId" // 서버의 DELETE API URL
        val request = Request.Builder()
            .url(deleteUrl)
            .delete() // HTTP DELETE 요청
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                callback(false) // 실패 시 false 반환
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful) // 성공 여부 반환
                if (!response.isSuccessful) {
                    Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show() // 문제 발생!
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