package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.android.synthetic.main.activity_school_search.*
import kotlinx.android.synthetic.main.search_bar.view.*


class SchoolSearchActivity : AppCompatActivity() {

    val schoolCodeMap = mutableMapOf<String, String>()
    val districtCodeMap = mutableMapOf<String, String>()

    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_search)

        val rows: List<List<String>> = csvReader().readAll(getAssets().open("school_info.csv"))

        val textList = mutableListOf<String>()

        for (i in 0 until rows.size) {
            var str: String = rows.get(i).toString()
            str = str.replace("[","")
            str = str.replace("]","")

            var strLst = str.split(",")


            schoolCodeMap.put(strLst.get(2).trim(), strLst.get(1).trim())
            districtCodeMap.put(strLst.get(2).trim(), strLst.get(0).trim())
            textList.add(strLst.get(2).trim())
        }

        // 자동 완성될 수 있도록 위의 textList를 어댑터로 만들어서 searchBar.autoCompleteTextView에 붙여준다.
        val adapter = ArrayAdapter<String>(
            this@SchoolSearchActivity,
            android.R.layout.simple_dropdown_item_1line, textList
        )
        searchBar.autoCompleteTextView.threshold = 1
        searchBar.autoCompleteTextView.setAdapter(adapter)
        searchBar.autoCompleteTextView.setText("")

        cancelButton.setOnClickListener {
            finish()
            val intent = Intent(this, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            val keyword = searchBar.autoCompleteTextView.text.toString()
            if(!schoolCodeMap.containsKey(keyword)) {
                Toast.makeText(applicationContext, "정확한 학교명을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                preference.edit().putString(Utils.schoolCodeKey, schoolCodeMap.get(keyword)).apply()
                preference.edit().putString(Utils.districtCodeKey, districtCodeMap.get(keyword)).apply()
                preference.edit().putString(Utils.schoolNameKey, keyword).apply()

                finish()
                val intent = Intent(this, MySettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }
}