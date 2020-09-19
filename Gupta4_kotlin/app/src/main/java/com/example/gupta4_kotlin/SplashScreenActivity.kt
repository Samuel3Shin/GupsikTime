package com.example.gupta4_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class SplashScrrenActivity: Activity() {
    val DURATION:Long = 1000
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({

            if(!preference.getBoolean(Utils.isSetSchoolKey, false)) {
                val intent = Intent(this, SchoolSearchActivity::class.java)
                intent.putExtra("isLanding", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }

        }, DURATION)
    }

    override fun onBackPressed() {
        // We don't want the splash screen to be interrupted
    }
}