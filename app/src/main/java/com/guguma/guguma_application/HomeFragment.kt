package com.guguma.guguma_application

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide


class HomeFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 해당 Fragment의 레이아웃을 인플레이트합니다.
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        // 이제 view.findViewById는 view가 초기화된 후 호출됩니다.
        val addPlantButton: Button = view.findViewById(R.id.plantBtn)


        // 버튼 클릭 리스너 설정
        addPlantButton.setOnClickListener {
            // Intent를 사용하여 AddPlantActivity를 엽니다.
            val intent = Intent(activity, testActivity::class.java)
            startActivity(intent)
        }

        return view  // return 문을 마지막에 위치시킵니다.
    }


}