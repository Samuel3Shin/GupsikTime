package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_my_allergy.*

class MyAllergyActivity : AppCompatActivity() {
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_allergy)
        var isLanding: Boolean = intent.getBooleanExtra("isLanding", false)

        if(isLanding) {
            cancelButton.visibility = View.INVISIBLE
            cancelButtonTextView.visibility = View.INVISIBLE
        }

        preference.getInt(Utils.myAllergy1Key, 0).let {
            if(it != 0) {
                image_checkbox_1.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy2Key, 0).let {
            if(it != 0) {
                image_checkbox_2.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy3Key, 0).let {
            if(it != 0) {
                image_checkbox_3.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy4Key, 0).let {
            if(it != 0) {
                image_checkbox_4.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy5Key, 0).let {
            if(it != 0) {
                image_checkbox_5.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy6Key, 0).let {
            if(it != 0) {
                image_checkbox_6.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy7Key, 0).let {
            if(it != 0) {
                image_checkbox_7.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy8Key, 0).let {
            if(it != 0) {
                image_checkbox_8.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy9Key, 0).let {
            if(it != 0) {
                image_checkbox_9.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy10Key, 0).let {
            if(it != 0) {
                image_checkbox_10.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy11Key, 0).let {
            if(it != 0) {
                image_checkbox_11.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy12Key, 0).let {
            if(it != 0) {
                image_checkbox_12.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy13Key, 0).let {
            if(it != 0) {
                image_checkbox_13.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy14Key, 0).let {
            if(it != 0) {
                image_checkbox_14.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy15Key, 0).let {
            if(it != 0) {
                image_checkbox_15.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy16Key, 0).let {
            if(it != 0) {
                image_checkbox_16.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy17Key, 0).let {
            if(it != 0) {
                image_checkbox_17.visibility = View.VISIBLE
            }
        }

        preference.getInt(Utils.myAllergy18Key, 0).let {
            if(it != 0) {
                image_checkbox_18.visibility = View.VISIBLE
            }
        }


        cancelButton.setOnClickListener {
            finish()
            val intent = Intent(this, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            if(image_checkbox_1.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy1Key, 1).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy1Key, 0).apply()
            }

            if(image_checkbox_2.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy2Key, 2).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy2Key, 0).apply()
            }

            if(image_checkbox_3.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy3Key, 3).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy3Key, 0).apply()
            }


            if(image_checkbox_4.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy4Key, 4).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy4Key, 0).apply()
            }

            if(image_checkbox_5.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy5Key, 5).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy5Key, 0).apply()
            }

            if(image_checkbox_6.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy6Key, 6).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy6Key, 0).apply()
            }

            if(image_checkbox_7.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy7Key, 7).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy7Key, 0).apply()
            }

            if(image_checkbox_8.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy8Key, 8).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy8Key, 0).apply()
            }

            if(image_checkbox_9.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy9Key, 9).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy9Key, 0).apply()
            }

            if(image_checkbox_10.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy10Key, 10).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy10Key, 0).apply()
            }

            if(image_checkbox_11.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy11Key, 11).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy11Key, 0).apply()
            }

            if(image_checkbox_12.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy12Key, 12).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy12Key, 0).apply()
            }

            if(image_checkbox_13.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy13Key, 13).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy13Key, 0).apply()
            }

            if(image_checkbox_14.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy14Key, 14).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy14Key, 0).apply()
            }

            if(image_checkbox_15.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy15Key, 15).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy15Key, 0).apply()
            }

            if(image_checkbox_16.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy16Key, 16).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy16Key, 0).apply()
            }

            if(image_checkbox_17.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy17Key, 17).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy17Key, 0).apply()
            }

            if(image_checkbox_18.visibility == View.VISIBLE) {
                preference.edit().putInt(Utils.myAllergy18Key, 18).apply()
            } else {
                preference.edit().putInt(Utils.myAllergy18Key, 0).apply()
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


        checkbox_1.setOnClickListener {
            Utils.toggleButton(image_checkbox_1)
        }
        text_checkbox_1.setOnClickListener {
            Utils.toggleButton(image_checkbox_1)
        }

        checkbox_2.setOnClickListener {
            Utils.toggleButton(image_checkbox_2)
        }
        text_checkbox_2.setOnClickListener {
            Utils.toggleButton(image_checkbox_2)
        }

        checkbox_3.setOnClickListener {
            Utils.toggleButton(image_checkbox_3)
        }
        text_checkbox_3.setOnClickListener {
            Utils.toggleButton(image_checkbox_3)
        }

        checkbox_4.setOnClickListener {
            Utils.toggleButton(image_checkbox_4)
        }
        text_checkbox_4.setOnClickListener {
            Utils.toggleButton(image_checkbox_4)
        }

        checkbox_5.setOnClickListener {
            Utils.toggleButton(image_checkbox_5)
        }
        text_checkbox_5.setOnClickListener {
            Utils.toggleButton(image_checkbox_5)
        }

        checkbox_6.setOnClickListener {
            Utils.toggleButton(image_checkbox_6)
        }
        text_checkbox_6.setOnClickListener {
            Utils.toggleButton(image_checkbox_6)
        }

        checkbox_7.setOnClickListener {
            Utils.toggleButton(image_checkbox_7)
        }
        text_checkbox_7.setOnClickListener {
            Utils.toggleButton(image_checkbox_7)
        }

        checkbox_8.setOnClickListener {
            Utils.toggleButton(image_checkbox_8)
        }
        text_checkbox_8.setOnClickListener {
            Utils.toggleButton(image_checkbox_8)
        }

        checkbox_9.setOnClickListener {
            Utils.toggleButton(image_checkbox_9)
        }
        text_checkbox_9.setOnClickListener {
            Utils.toggleButton(image_checkbox_9)
        }

        checkbox_10.setOnClickListener {
            Utils.toggleButton(image_checkbox_10)
        }
        text_checkbox_10.setOnClickListener {
            Utils.toggleButton(image_checkbox_10)
        }

        checkbox_11.setOnClickListener {
            Utils.toggleButton(image_checkbox_11)
        }
        text_checkbox_11.setOnClickListener {
            Utils.toggleButton(image_checkbox_11)
        }

        checkbox_12.setOnClickListener {
            Utils.toggleButton(image_checkbox_12)
        }
        text_checkbox_12.setOnClickListener {
            Utils.toggleButton(image_checkbox_12)
        }

        checkbox_13.setOnClickListener {
            Utils.toggleButton(image_checkbox_13)
        }
        text_checkbox_13.setOnClickListener {
            Utils.toggleButton(image_checkbox_13)
        }

        checkbox_14.setOnClickListener {
            Utils.toggleButton(image_checkbox_14)
        }
        text_checkbox_14.setOnClickListener {
            Utils.toggleButton(image_checkbox_14)
        }

        checkbox_15.setOnClickListener {
            Utils.toggleButton(image_checkbox_15)
        }
        text_checkbox_15.setOnClickListener {
            Utils.toggleButton(image_checkbox_15)
        }

        checkbox_16.setOnClickListener {
            Utils.toggleButton(image_checkbox_16)
        }
        text_checkbox_16.setOnClickListener {
            Utils.toggleButton(image_checkbox_16)
        }

        checkbox_17.setOnClickListener {
            Utils.toggleButton(image_checkbox_17)
        }
        text_checkbox_17.setOnClickListener {
            Utils.toggleButton(image_checkbox_17)
        }

        checkbox_18.setOnClickListener {
            Utils.toggleButton(image_checkbox_18)
        }
        text_checkbox_18.setOnClickListener {
            Utils.toggleButton(image_checkbox_18)
        }
    }
}