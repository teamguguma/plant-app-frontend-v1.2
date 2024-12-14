package com.guguma.guguma_application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.guguma.guguma_application.databinding.FragmentHomeBinding
import okhttp3.*
import org.json.JSONArray
import android.util.Log
import android.widget.Toast
import com.guguma.guguma_application.dto.PlantDto
import java.io.IOException

class HomeFragment : Fragment() {

    private val client = OkHttpClient()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 버튼 클릭 리스너 설정
        binding.plantBtn.setOnClickListener {
            // Intent를 사용하여 AddPlantActivity를 엽니다.
            val intent = Intent(activity, testActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_PLANT)
        }

        fetchPlantsFromServer() // 데이터를 백엔드에서 가져옵니다.

        return binding.root

    }

    // onActivityResult 메서드 추가
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_PLANT && resultCode == Activity.RESULT_OK) {
            val newPlantName = data?.getStringExtra("newPlantName")
            val newPlantNickname = data?.getStringExtra("newPlantNickname")
            val newPlantImageUrl = data?.getStringExtra("newPlantImageUrl")

            if (newPlantName != null && newPlantNickname != null && newPlantImageUrl != null) {
                val newPlant = PlantDto(newPlantName, newPlantNickname, newPlantImageUrl)
                addPlantToList(newPlant) // 리스트뷰에 추가
            }
        }
    }

    private fun addPlantToList(newPlant: PlantDto) {
        (binding.plantListView.adapter as PlantAdapter).apply {
            plantList.add(newPlant) // 새 데이터 추가
            notifyDataSetChanged() // 어댑터 갱신
        }
    }

    private fun fetchPlantsFromServer() {
        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_LIST) // URL을 직접 전달
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("HomeFragment", "Failed to fetch plants: ${res.message}")
                        return
                    }

                    val responseData = res.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        val plantList = parsePlantData(responseData).toMutableList() // 변경점
                        activity?.runOnUiThread {
                            updateUI(plantList)
                        }
                    } else {
                        Log.e("HomeFragment", "Empty response")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeFragment", "Request failed: ${e.message}", e)
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Error fetching data", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun parsePlantData(json: String): List<PlantDto> {
        val jsonArray = JSONArray(json)
        val plantList = mutableListOf<PlantDto>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getString("name")
            val nickname = item.getString("nickname")
            val imageUrl = item.getString("imageUrl")
            plantList.add(PlantDto(name, nickname, imageUrl))
        }

        return plantList
    }

    private fun updateUI(plantList: MutableList<PlantDto>) {
        val listView = binding.plantListView
        listView.adapter = PlantAdapter(requireContext(), plantList)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null //메모리 누수 방지
    }

    companion object {
        const val REQUEST_ADD_PLANT = 1001 // Request code for AddPlantActivity
    }

}