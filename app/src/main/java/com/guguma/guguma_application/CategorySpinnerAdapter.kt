package com.guguma.guguma_application

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import android.content.Context

class CategorySpinnerAdapter(
    context: Context,
    @LayoutRes private val resId: Int,
    private val categoryList: List<String>
) : ArrayAdapter<String>(context, resId, categoryList) {

    // 드롭다운하지 않은 상태의 Spinner 항목 뷰
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(resId, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewAddPlantSpinnerItem)
        textView.text = categoryList[position]
        return view
    }

    // 드롭다운된 항목들 리스트의 뷰
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(resId, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewAddPlantSpinnerItem)
        textView.text = categoryList[position]
        return view
    }

    override fun getCount() = categoryList.size
}

