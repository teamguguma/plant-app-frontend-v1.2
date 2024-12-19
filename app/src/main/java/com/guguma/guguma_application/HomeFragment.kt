package com.guguma.guguma_application

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.guguma.guguma_application.databinding.FragmentHomeBinding
import com.guguma.guguma_application.dto.PlantDto
import com.guguma.guguma_application.viewmodel.PlantViewModel
import com.guguma.guguma_application.viewmodel.PlantViewModelFactory

class HomeFragment : Fragment() {
    companion object {
        const val REQUEST_ADD_PLANT = 1001 // 요청 코드
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val plantViewModel: PlantViewModel by lazy {
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("userUuid", null) ?: run {
            Toast.makeText(requireContext(), "User ID not found. Setting default ID.", Toast.LENGTH_SHORT).show()
            "default_user_id" // 기본 userId 제공
        }
        val factory = PlantViewModelFactory(userId)
        ViewModelProvider(this, factory).get(PlantViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observePlantList()
        setupAddPlantButton()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.plantListView.layoutManager = LinearLayoutManager(requireContext())
        binding.plantListView.adapter = PlantAdapter(requireContext(), mutableListOf()) { plantId ->
            plantViewModel.deletePlant(plantId)
        }
    }

    private fun observePlantList() {
        plantViewModel.plantList.observe(viewLifecycleOwner) { updatedPlantList ->
            updateUI(updatedPlantList)
        }
    }

    private fun setupAddPlantButton() {
        binding.pBtn.setOnClickListener {
            val intent = Intent(activity, CreatePlantStartActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_PLANT)
        }
    }

    override fun onResume() {
        super.onResume()
        plantViewModel.fetchPlantsFromServer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_PLANT && resultCode == Activity.RESULT_OK) {
            handleAddPlantResult(data)
        }
    }

    private fun handleAddPlantResult(data: Intent?) {
        val plantId = data?.getLongExtra("plantId", -1L) ?: -1L
        val newPlantName = data?.getStringExtra("newPlantName")
        val newPlantNickname = data?.getStringExtra("newPlantNickname")
        val newPlantImageUrl = data?.getStringExtra("newPlantImageUrl")

        if (plantId != -1L && !newPlantName.isNullOrEmpty() && !newPlantNickname.isNullOrEmpty() && !newPlantImageUrl.isNullOrEmpty()) {
            val newPlant = PlantDto(plantId, newPlantName, newPlantNickname, newPlantImageUrl)
            plantViewModel.addPlant(newPlant)
        }
    }

    private fun updateUI(plantList: MutableList<PlantDto>) {
        val adapter = binding.plantListView.adapter
        if (adapter is PlantAdapter) {
            adapter.updateData(plantList)
        } else {
            binding.plantListView.adapter = PlantAdapter(requireContext(), plantList) { plantId ->
                plantViewModel.deletePlant(plantId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}