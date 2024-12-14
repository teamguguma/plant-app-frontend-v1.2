package com.guguma.guguma_application.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "plantlist")
class PlantList (
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "plantName") val PlantName: String,
    @ColumnInfo(name = "timestamp") val timestamp: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "imageUrl") val imageUrl: String
): Serializable{
}

//id: 기본키 autoGenerate = true 를 해주었기 때문에, id 값을 자동으로 증가
//plantName: 식물종 이름
//timestamp: 생성/수정 날짜
//nickname: 식물 애칭
//imageUrl: 식물 사진(유저가 올린/찍은)
//Intent 에 객체를 담기 위해 Serializable을 상속
//ref:https://pekahblog.tistory.com/169