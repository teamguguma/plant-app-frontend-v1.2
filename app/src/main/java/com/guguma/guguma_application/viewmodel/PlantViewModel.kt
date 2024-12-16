package com.guguma.guguma_application.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guguma.guguma_application.BuildConfig
import com.guguma.guguma_application.dto.PlantDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.IOException

class PlantViewModel : ViewModel() {
    private val _plantList = MutableLiveData<MutableList<PlantDto>>()
    val plantList: LiveData<MutableList<PlantDto>> = _plantList

    private val client = OkHttpClient()

    init {
        fetchPlantsFromServer()
    }

    // 서버에서 식물 목록 가져오기
    private fun fetchPlantsFromServer() {
        val request = Request.Builder()
            .url(BuildConfig.API_PLANT_LIST) // 서버 URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("PlantViewModel", "Failed to fetch plants: ${res.message}")
                        return
                    }
                    val responseData = res.body?.string()
                    if (!responseData.isNullOrEmpty()) {
                        val plants = parsePlantData(responseData).toMutableList()
                        Log.d("PlantViewModel", "Fetched plants: $plants")
                        _plantList.postValue(plants) // LiveData 갱신
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlantViewModel", "Error fetching plants: ${e.message}")
            }
        })
    }

    // 식물 추가 및 서버 데이터 재동기화
    fun addPlantAndRefresh(newPlant: PlantDto) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonBody = """
            {
                "name": "${newPlant.name}",
                "nickname": "${newPlant.nickname}",
                "imageUrl": "${newPlant.imageUrl}",
                "user": { "id": 1 }
            }
        """.trimIndent()
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(BuildConfig.API_PLANT_REGISTER)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("PlantViewModel", "Plant added successfully: $newPlant")
                        fetchPlantsFromServer() // 추가 후 데이터 갱신
                    } else {
                        Log.e("PlantViewModel", "Failed to add plant: ${response.message}")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PlantViewModel", "Failed to add plant: ${e.message}")
                }
            })
        }
    }

    // 식물 삭제
    fun deletePlant(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${BuildConfig.API_PLANT_DELETE.replace("{id}", id.toString())}")
                .delete()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        fetchPlantsFromServer() // 삭제 후 데이터 갱신
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    // 에러 처리
                }
            })
        }
    }

    // JSON 데이터를 PlantDto 객체로 변환
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
}
