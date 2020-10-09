package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.android.synthetic.main.activity_school_search.*
import kotlinx.android.synthetic.main.search_bar.view.*


class SchoolSearchActivity : AppCompatActivity() {

    private val schoolCodeMap = mutableMapOf<String, String>()
    private val districtCodeMap = mutableMapOf<String, String>()

    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_search)

        val isLanding: Boolean = intent.getBooleanExtra("isLanding", false)

        if(isLanding) {
            cancelButton.visibility = View.INVISIBLE
            cancelButtonTextView.visibility = View.INVISIBLE
        }

        val rows: List<List<String>> = csvReader().readAll(assets.open("school_info.csv"))

        val textList = mutableListOf<String>()

        for (i in rows.indices) {
            schoolCodeMap[rows[i][2].trim()] = rows[i][1].trim()
            districtCodeMap[rows[i][2].trim()] = rows[i][0].trim()
            textList.add(rows[i][2].trim())
        }

        // 자동 완성될 수 있도록 위의 textList를 어댑터로 만들어서 searchBar.autoCompleteTextView에 붙여준다.
        val adapter = ArrayAdapter(
            this@SchoolSearchActivity,
            android.R.layout.simple_dropdown_item_1line, textList
        )
        searchBar.autoCompleteTextView.threshold = 1
        searchBar.autoCompleteTextView.setAdapter(adapter)
        searchBar.autoCompleteTextView.setText("")

        cancelButton.setOnClickListener {
            finish()
            val intent = Intent(applicationContext, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            val keyword = searchBar.autoCompleteTextView.text.toString()
            if(!schoolCodeMap.containsKey(keyword)) {
                Toast.makeText(applicationContext, "정확한 학교명을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                preference.edit().putString(Utils.schoolCodeKey, schoolCodeMap[keyword]).apply()
                preference.edit().putString(Utils.districtCodeKey, districtCodeMap[keyword]).apply()
                preference.edit().putString(Utils.schoolNameKey, keyword).apply()

                finish()

                if(isLanding) {
                    val intent = Intent(applicationContext, MyAllergyActivity::class.java)
                    intent.putExtra("isLanding", true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                } else {
                    val intent = Intent(applicationContext, MySettingActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }

            }

            Toast.makeText(applicationContext, "힉교 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        searchBar.autoCompleteTextView.setAdapter(null)
        super.onDestroy()
    }
}