package com.example.gupta4_kotlin

import android.content.Context
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
        boardKey = boardKey + "/$schoolCode"

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
    }
}