package com.guguma.guguma_application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guguma.guguma_application.databinding.FragmentHomeBinding
import com.guguma.guguma_application.dto.PlantDto

//import com.guguma.guguma_application.viewmodel.PlantViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
//    private val plantViewModel: PlantViewModel by activityViewModels()

    companion object {
        const val REQUEST_ADD_PLANT = 1001 // AddPlantActivity의 요청 코드
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // LiveData를 observe하여 UI 업데이트
//        plantViewModel.plantList.observe(viewLifecycleOwner) { updatedPlantList ->
//            updateUI(updatedPlantList)
//        }

        // 식물 추가 버튼 클릭 리스너
        binding.plantBtn.setOnClickListener {
            val intent = Intent(activity, CreatePlantStartActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_PLANT)
        }

        return binding.root

    }

    // AddPlantActivity에서 돌아왔을 때 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_PLANT && resultCode == Activity.RESULT_OK) {
            val newPlantName = data?.getStringExtra("newPlantName")
            val newPlantNickname = data?.getStringExtra("newPlantNickname")
            val newPlantImageUrl = data?.getStringExtra("newPlantImageUrl")

            if (newPlantName != null && newPlantNickname != null && newPlantImageUrl != null) {
                val newPlant = PlantDto(newPlantName, newPlantNickname, newPlantImageUrl)
                Log.d("HomeFragment", "Adding new plant: $newPlant")

                // ViewModel에 데이터 추가 및 갱신 요청
//                plantViewModel.addPlantAndRefresh(newPlant)
            }
        }
    }

    // UI 업데이트 메서드
    private fun updateUI(plantList: MutableList<PlantDto>) {
        val adapter = binding.plantListView.adapter as? PlantAdapter
        if (adapter == null) {
            // 어댑터가 없으면 새로 생성
            binding.plantListView.adapter = PlantAdapter(requireContext(), plantList)
        } else {
            // 어댑터가 이미 존재하면 데이터 갱신
            adapter.updateData(plantList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }

}