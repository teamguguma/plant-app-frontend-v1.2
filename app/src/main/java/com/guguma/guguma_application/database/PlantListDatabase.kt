package com.guguma.guguma_application.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guguma.guguma_application.dao.PlantListDao
import com.guguma.guguma_application.dto.PlantList

@Database(entities = arrayOf(PlantList::class), version = 1)
abstract class PlantListDatabase: RoomDatabase() {
    abstract fun plantlistDao(): PlantListDao
}

//entity는 PlantList 클래스로, RoomDatabase 라이브러리를 사용해 생성