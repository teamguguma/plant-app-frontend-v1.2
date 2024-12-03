package com.guguma.guguma_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent


class InfoFragment : Fragment() {

    lateinit var infoAdapter: InfoAdapter
    val datas = mutableListOf<InfoData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoAdapter = InfoAdapter(requireContext()) { item ->
            val intent = Intent(requireContext(), NewActivity::class.java) // NewActivity로 이동
            intent.putExtra("itemName", item.name) // 필요한 데이터를 전달
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = infoAdapter

        initData()
    }


    private fun initData() {

        datas.apply {
            add(InfoData(name="데이터 옮기기"))
            add(InfoData(name="알림"))
            add(InfoData(name="앱 테마"))
            add(InfoData(name="건의하기"))
        }
        infoAdapter.datas.addAll(datas)
        infoAdapter.notifyDataSetChanged()
    }




}