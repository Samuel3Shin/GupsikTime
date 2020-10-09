package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_my_allergy.*

class MyAllergyActivity : AppCompatActivity() {

    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}
    private val allergyKeyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_allergy)
        val isLanding: Boolean = intent.getBooleanExtra("isLanding", false)

        allergyKeyList.addAll(listOf("", Utils.myAllergy1Key, Utils.myAllergy2Key, Utils.myAllergy3Key, Utils.myAllergy4Key, Utils.myAllergy5Key, Utils.myAllergy6Key,
            Utils.myAllergy7Key, Utils.myAllergy8Key, Utils.myAllergy9Key, Utils.myAllergy10Key, Utils.myAllergy11Key, Utils.myAllergy12Key, Utils.myAllergy13Key,
            Utils.myAllergy14Key, Utils.myAllergy15Key, Utils.myAllergy16Key, Utils.myAllergy17Key, Utils.myAllergy18Key))

        val imageCheckboxMap: MutableMap<String, View> = mutableMapOf()
        val textCheckboxMap: MutableMap<String, View> = mutableMapOf()
        val checkboxMap: MutableMap<String, View> = mutableMapOf()

        val childCnt: Int = allergyFrame.childCount
        for(i in 0 until childCnt) {
            val viewId = allergyFrame.getChildAt(i).resources.getResourceEntryName(allergyFrame.getChildAt(i).id).toString()

            when {
                viewId.startsWith("checkbox_") -> {
                    checkboxMap[allergyKeyList[viewId.split("checkbox_")[1].toInt()]] = allergyFrame.getChildAt(i)
                }
                viewId.startsWith("image_checkbox_") -> {
                    imageCheckboxMap[allergyKeyList[viewId.split("image_checkbox_")[1].toInt()]] =
                        allergyFrame.getChildAt(i)
                }
                viewId.startsWith("text_checkbox_") -> {
                    textCheckboxMap[allergyKeyList[viewId.split("text_checkbox_")[1].toInt()]] =
                        allergyFrame.getChildAt(i)
                }
            }
        }

        if(isLanding) {
            cancelButton.visibility = View.INVISIBLE
            cancelButtonTextView.visibility = View.INVISIBLE
        }


        for(i in 1 until allergyKeyList.size) {
            // 알러지 체크했던 것들은 체크한 걸로 표시!
            preference.getInt(allergyKeyList[i], 0).let {
                if(it != 0) {
                    imageCheckboxMap[allergyKeyList[i]]!!.visibility = View.VISIBLE
                }
            }

            //checkbox 클릭되면 토글!
            checkboxMap[allergyKeyList[i]]!!.setOnClickListener {
                Utils.toggleButton(imageCheckboxMap[allergyKeyList[i]]!!)
            }

            // text_checkbox 클릭되어도 토글!
            textCheckboxMap[allergyKeyList[i]]!!.setOnClickListener {
                Utils.toggleButton(imageCheckboxMap[allergyKeyList[i]]!!)
            }
        }

        cancelButton.setOnClickListener {
            finish()
            val intent = Intent(applicationContext, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            for(i in 1 until allergyKeyList.size) {
                if(imageCheckboxMap[allergyKeyList[i]]!!.visibility == View.VISIBLE) {
                    preference.edit().putInt(allergyKeyList[i], i).apply()
                } else {
                    preference.edit().putInt(allergyKeyList[i], 0).apply()
                }

            }

            finish()

            if(isLanding) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                preference.edit().putBoolean(Utils.isSetSchoolKey, true).apply()

                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, MySettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            Toast.makeText(applicationContext, "알러지 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show()

        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}