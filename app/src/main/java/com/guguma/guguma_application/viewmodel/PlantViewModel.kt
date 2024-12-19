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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class PlantViewModel(private val userId: String) : ViewModel() {
    private val _plantList = MutableLiveData<MutableList<PlantDto>>()
    val plantList: LiveData<MutableList<PlantDto>> = _plantList
    private val client = OkHttpClient()

    init {
        fetchPlantsFromServer()
    }

    val userUuid = userId
    fun fetchPlantsFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = getPlantListUrl(userUuid)
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                response.use { // 리소스를 안전하게 닫기 위해 use 사용
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        responseData?.let {
                            val plants = parsePlantData(it).toMutableList()
                            _plantList.postValue(plants)
                        }
                    } else {
                        Log.e("PlantViewModel", "Failed to fetch plants: ${response.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e("PlantViewModel", "Error fetching plants: ${e.message}")
            }
        }
    }

    fun deletePlant(plantId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = getPlantDeleteUrl(plantId)
                val request = Request.Builder().url(url).delete().build()
                val response = client.newCall(request).execute()

                response.use {
                    if (response.isSuccessful) {
                        fetchPlantsFromServer() // 삭제 후 데이터 갱신
                    } else {
                        Log.e("PlantViewModel", "Failed to delete plant: ${response.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e("PlantViewModel", "Error deleting plant: ${e.message}")
            }
        }
    }

    fun addPlant(plant: PlantDto) {
        val currentList = _plantList.value ?: mutableListOf()
        currentList.add(plant)
        _plantList.postValue(currentList)
    }

    private fun parsePlantData(json: String): List<PlantDto> {
        val jsonArray = JSONArray(json)
        val plantList = mutableListOf<PlantDto>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)

            val id = item.getLong("id")
            val name = item.getString("name")
            val nickname = item.getString("nickname")
            val imageUrl = item.getString("imageUrl")

            plantList.add(PlantDto(id, name, nickname, imageUrl))
        }

        return plantList
    }

    private fun getPlantListUrl(userId: String): String {
        return "${BuildConfig.BASE_URL}/plants/user/$userId"
    }

    private fun getPlantDeleteUrl(plantId: Long): String {
        return "${BuildConfig.BASE_URL}/plants/delete/$plantId"
    }
}