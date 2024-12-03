package com.guguma.guguma_application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel(){
    private val _plantAdded = MutableLiveData<Boolean>()
    val plantAdded: LiveData<Boolean> get() = _plantAdded

    fun addPlant() {
        _plantAdded.value = true
    }

    fun resetPlantAdded() {
        _plantAdded.value = false
    }
}