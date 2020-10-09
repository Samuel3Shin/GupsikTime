package com.gooselab.gupta4_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashScrrenActivity: Activity() {
    private val duration:Long = 1000
    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({

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

        }, duration)
    }

    override fun onBackPressed() {
        // We don't want the splash screen to be interrupted
    }
}