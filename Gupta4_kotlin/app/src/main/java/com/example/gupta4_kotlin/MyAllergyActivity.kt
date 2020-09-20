package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_my_allergy.*

class MyAllergyActivity : AppCompatActivity() {
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}
    val allergyKeyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_allergy)
        var isLanding: Boolean = intent.getBooleanExtra("isLanding", false)

        allergyKeyList.addAll(listOf("", Utils.myAllergy1Key, Utils.myAllergy2Key, Utils.myAllergy3Key, Utils.myAllergy4Key, Utils.myAllergy5Key, Utils.myAllergy6Key,
            Utils.myAllergy7Key, Utils.myAllergy8Key, Utils.myAllergy9Key, Utils.myAllergy10Key, Utils.myAllergy11Key, Utils.myAllergy12Key, Utils.myAllergy13Key,
            Utils.myAllergy14Key, Utils.myAllergy15Key, Utils.myAllergy16Key, Utils.myAllergy17Key, Utils.myAllergy18Key))

        var imageCheckboxMap: MutableMap<String, View> = mutableMapOf()
        var textCheckboxMap: MutableMap<String, View> = mutableMapOf()
        var checkboxMap: MutableMap<String, View> = mutableMapOf()

        var childCnt: Int = allergyFrame.getChildCount()
        for(i in 0 until childCnt) {
            var viewId = allergyFrame.getChildAt(i).getResources().getResourceEntryName(allergyFrame.getChildAt(i).id).toString()
            if(viewId.startsWith("checkbox_")) {
                checkboxMap.put(allergyKeyList.get(viewId.split("checkbox_").get(1).toInt()), allergyFrame.getChildAt(i))
            } else if(viewId.startsWith("image_checkbox_")) {
                imageCheckboxMap.put(allergyKeyList.get(viewId.split("image_checkbox_").get(1).toInt()), allergyFrame.getChildAt(i))
            }  else if(viewId.startsWith("text_checkbox_")) {
                textCheckboxMap.put(allergyKeyList.get(viewId.split("text_checkbox_").get(1).toInt()), allergyFrame.getChildAt(i))
            }
        }

        if(isLanding) {
            cancelButton.visibility = View.INVISIBLE
            cancelButtonTextView.visibility = View.INVISIBLE
        }


        for(i in 1 until allergyKeyList.size) {
            // 알러지 체크했던 것들은 체크한 걸로 표시!
            preference.getInt(allergyKeyList.get(i), 0).let {
                if(it != 0) {
                    imageCheckboxMap.get(allergyKeyList.get(i))!!.visibility = View.VISIBLE
                }
            }

            //checkbox 클릭되면 토글!
            checkboxMap.get(allergyKeyList.get(i))!!.setOnClickListener {
                Utils.toggleButton(imageCheckboxMap.get(allergyKeyList.get(i))!!)
            }

            // text_checkbox 클릭되어도 토글!
            textCheckboxMap.get(allergyKeyList.get(i))!!.setOnClickListener {
                Utils.toggleButton(imageCheckboxMap.get(allergyKeyList.get(i))!!)
            }
        }

        cancelButton.setOnClickListener {
            finish()
            val intent = Intent(this, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            for(i in 1 until allergyKeyList.size) {
                if(imageCheckboxMap.get(allergyKeyList.get(i))!!.visibility == View.VISIBLE) {
                    preference.edit().putInt(allergyKeyList.get(i), i).apply()
                } else {
                    preference.edit().putInt(allergyKeyList.get(i), 0).apply()
                }

            }

            finish()

            if(isLanding) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                preference.edit().putBoolean(Utils.isSetSchoolKey, true).apply()
                startActivity(intent)
            } else {
                val intent = Intent(this, MySettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

        }

    }
}