package com.example.gupta4_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_community.buttonUpper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.gupsik_post.view.*

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