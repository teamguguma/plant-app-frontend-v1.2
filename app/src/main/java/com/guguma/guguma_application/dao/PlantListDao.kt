package com.guguma.guguma_application.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.guguma.guguma_application.dto.PlantList

@Dao
interface PlantListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dto: PlantList)

    @Query("select * from plantlist")
    fun list(): LiveData<MutableList<PlantList>>

    @Query("select * from plantlist where id = (:id)")
    fun selectOne(id: Long): PlantList

    @Update
    suspend fun update(dto: PlantList)

    @Delete
    fun delete(dto: PlantList)
}

//*
// CRUD 작성
// insert, query, update, delete는 Room 어노테이션을 사용해 구성
//모든 항목을 불러오는 list 함수의 경우 LiveData를 사용해, 추가, 수정, 삭제에 의해 변화하는 값에 대해 즉시 반영이 가능
//
// *
//





