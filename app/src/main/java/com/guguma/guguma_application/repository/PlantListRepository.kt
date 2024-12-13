package com.guguma.guguma_application.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.guguma.guguma_application.database.PlantListDatabase
import com.guguma.guguma_application.dto.PlantList

private const val DATABASE_NAME = "plantlist-database.db"
class PlantListRepository private constructor(context: Context){

    private val database: PlantListDatabase = Room.databaseBuilder(
        context.applicationContext,
        PlantListDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val plantlistDao = database.plantlistDao()

    fun list(): LiveData<MutableList<PlantList>> = plantlistDao.list()

    fun getPlantList(id: Long): PlantList = plantlistDao.selectOne(id)

    fun insert(dto: PlantList) = plantlistDao.insert(dto)

    suspend fun update(dto: PlantList) = plantlistDao.update(dto)

    fun delete(dto: PlantList) = plantlistDao.delete(dto)

    companion object {
        private var INSTANCE: PlantListRepository?=null

        fun initialize(context: Context) {
            if (INSTANCE == null){
                INSTANCE = PlantListRepository(context)
            }
        }

        fun get(): PlantListRepository{
            return INSTANCE ?:
            throw IllegalStateException("PlantListRepository must be initialized")
        }
    }
}

//먼저 Room.databaseBuilder().build() 를 통해 데이터베이스를 빌드하도록 한다.
//companion object 객체는 클래스가 생성될 때 메모리에 적재되면서 동시에 생성하는 객체로, 데이터베이스 생성 및 초기화를 담당하기 위해 작성