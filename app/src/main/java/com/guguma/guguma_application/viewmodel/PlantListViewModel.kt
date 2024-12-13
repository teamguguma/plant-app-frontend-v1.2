package com.guguma.guguma_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guguma.guguma_application.dto.PlantList
import com.guguma.guguma_application.repository.PlantListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlantListViewModel : ViewModel() {
    val plantList: LiveData<MutableList<PlantList>>
    private val plantlistRepository: PlantListRepository = PlantListRepository.get()

    init {
        plantList = plantlistRepository.list()
    }

    fun getOne(id: Long) = plantlistRepository.getPlantList(id)

    fun insert(dto: PlantList) = viewModelScope.launch(Dispatchers.IO) {
        plantlistRepository.insert(dto)
    }

    fun update(dto: PlantList) = viewModelScope.launch (Dispatchers.IO) {
        plantlistRepository.update(dto)
    }

    fun delete(dto: PlantList) = viewModelScope.launch (Dispatchers.IO) {
        plantlistRepository.delete(dto)
    }
}

//viewModel은 액티비티의 라이프사이클과 별개로 돌아가기 때문에 데이터의 유지 및 공유가 가능
// 이에 따라 viewModel에서 CRUD를 사용해 액티비티의 이동이 있어도 동일하게 값을 불러올 수 있도록 함.