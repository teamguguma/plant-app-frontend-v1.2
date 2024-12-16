package com.guguma.guguma_application

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.guguma.guguma_application.dto.PlantDto

class PlantAdapter(private val context: Context, private val plantList: MutableList<PlantDto>) : BaseAdapter() {

    override fun getCount(): Int = plantList.size

    override fun getItem(position: Int): Any = plantList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_plant, parent, false)

        val plant = plantList[position]
        val nameTextView = view.findViewById<TextView>(R.id.plantNameTextView)
        val nicknameTextView = view.findViewById<TextView>(R.id.plantNicknameTextView)
        val imageView = view.findViewById<ImageView>(R.id.plantImageView)

        nameTextView.text = plant.name
        nicknameTextView.text = plant.nickname
        Glide.with(context).load(plant.imageUrl).into(imageView)

        return view
    }

    // 외부에서 데이터를 갱신할 수 있는 메서드 추가
    fun updateData(newPlantList: List<PlantDto>) {
        plantList.clear()
        plantList.addAll(newPlantList)
        notifyDataSetChanged() // 데이터 변경 후 UI 갱신
    }

}