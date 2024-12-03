package com.guguma.guguma_application

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder


class InfoAdapter (private val context: Context, private val onItemClicked: (InfoData) -> Unit) :
RecyclerView.Adapter<InfoAdapter.ViewHolder>() {

    val datas = mutableListOf<InfoData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            InfoAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.info_item_recycler,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: InfoAdapter.ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val txtName: TextView = itemView.findViewById(R.id.info_item_name)

        fun bind(item: InfoData) {
            txtName.text = item.name

            itemView.setOnClickListener{
                onItemClicked(item)
            }
        }
    }

}

//https://yunaaaas.tistory.com/43 블로그의 코드를 참고함..
