package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_community.*

class CommunityMySchoolActivity : CommunityActivity() {
    override var boardKey = "mySchool"
    var schoolCode = ""
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        schoolCode = preference.getString(Utils.schoolCodeKey, "").toString()

        // boardKey에 schoolCode 추가
        boardKey += "/$schoolCode"

        super.onCreate(savedInstanceState)

        // 버튼 토글해주는 거 추가
        Utils.toggleButton(mySchoolButton)
        Utils.toggleButton(mySchoolPressedButton)

        if(bambooButton.visibility == View.INVISIBLE) {
            Utils.toggleButton(bambooButton)
            Utils.toggleButton(bambooPressedButton)
        } else if(careerButton.visibility == View.INVISIBLE) {
            Utils.toggleButton(careerButton)
            Utils.toggleButton(careerPressedButton)
        }

        // 게시판 설명 부분변경
        boardDescribeTextView.text = "우리 학교만 쓰는 게시판"
        leftImageView.setImageResource(R.drawable.ic_openmoji_school)
        rightImageView.setImageResource(R.drawable.ic_openmoji_school)

    }
}