package com.example.gupta4_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_community.*

class CommunityCareerActivity: CommunityActivity() {
    override var boardKey = "career"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.toggleButton(careerButton)
        Utils.toggleButton(careerPressedButton)

        if(bambooButton.visibility == View.INVISIBLE) {
            Utils.toggleButton(bambooButton)
            Utils.toggleButton(bambooPressedButton)
        } else if(mySchoolButton.visibility == View.INVISIBLE) {
            Utils.toggleButton(mySchoolButton)
            Utils.toggleButton(mySchoolPressedButton)
        }

        // 게시판 설명 부분변경
        boardDescribeTextView.text = "전국 진로고민 상담소"
        leftImageView.setImageResource(R.drawable.ic_twemoji_thinking_face)
        rightImageView.setImageResource(R.drawable.ic_twemoji_thinking_face)

    }

}