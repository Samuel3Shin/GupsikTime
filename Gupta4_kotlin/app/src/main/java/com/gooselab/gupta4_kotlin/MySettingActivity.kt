package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_community.buttonUpper
import kotlinx.android.synthetic.main.activity_my_setting.*
import kotlinx.android.synthetic.main.activity_my_setting.adView

class MySettingActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}
    private val allergyList: MutableList<String> = mutableListOf()
    private val allergyKeyList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_setting)

        //배너 광고 추가
        MobileAds.initialize(applicationContext, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        mySchoolInfoTextView.text = preference.getString(Utils.schoolNameKey, "")


        allergyList.addAll(listOf("", "난류", "우유", "메밀", "땅콩", "대두", "밀", "고등어", "게", "새우", "돼지고기", "복숭아", "토마토", "아황산염", "호두", "닭고기", "쇠고기", "오징어", "조개류"))
        allergyKeyList.addAll(listOf(Utils.myAllergy1Key, Utils.myAllergy2Key, Utils.myAllergy3Key, Utils.myAllergy4Key, Utils.myAllergy5Key, Utils.myAllergy6Key,
            Utils.myAllergy7Key, Utils.myAllergy8Key, Utils.myAllergy9Key, Utils.myAllergy10Key, Utils.myAllergy11Key, Utils.myAllergy12Key, Utils.myAllergy13Key,
            Utils.myAllergy14Key, Utils.myAllergy15Key, Utils.myAllergy16Key, Utils.myAllergy17Key, Utils.myAllergy18Key))
        var allergyInfo = ""

        for(i in 0 until allergyKeyList.size) {
            allergyList[preference.getInt(allergyKeyList[i], 0)].let {
                if(it!= "") {
                    allergyInfo = "$allergyInfo$it, "
                }
            }
        }

        if(allergyInfo.isNotEmpty()) {
            allergyInfo = allergyInfo.substring(0, allergyInfo.length-2)
        }

        myAllergyInfoTextView.text = allergyInfo

        myPostsTab.setOnClickListener {
            val intent = Intent(applicationContext, MyPostsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@MySettingActivity, it)
            popup.setOnMenuItemClickListener(this@MySettingActivity)
            popup.inflate(R.menu.main)
            popup.show()

        }

        mySchoolInfoEditButton.setOnClickListener {
            val intent = Intent(applicationContext, SchoolSearchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        myAllergyInfoEditButton.setOnClickListener {
            val intent = Intent(applicationContext, MyAllergyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            startActivity(intent)

            finish()

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_board ->  {
                val intent = Intent(applicationContext, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_myPage ->  {

                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

    // Called when leaving the activity
    public override fun onPause() {
        adView.pause()
        super.onPause()
    }

    // Called when returning to the activity
    public override fun onResume() {
        super.onResume()
        adView.resume()
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}