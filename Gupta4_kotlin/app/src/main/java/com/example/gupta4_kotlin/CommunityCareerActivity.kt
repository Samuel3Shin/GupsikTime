package com.example.gupta4_kotlin

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

    }

}