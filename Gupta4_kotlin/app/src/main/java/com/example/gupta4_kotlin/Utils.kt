package com.example.gupta4_kotlin

import android.content.Context
import android.view.View
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    val isSetSchoolKey = "isSetSchool"
    val districtCodeKey = "districtCode"
    val schoolCodeKey = "schoolCode"
    val schoolNameKey = "schoolName"
    val myPostIdsKey = "myPostIds"
    val myCommentIdsKey = "myCommentIds"
    val myLikedPostIdsKey = "myLikedPostIds"
    val isAllergyInfoInsertedKey = "isAllergyInfoInserted"
    val myAllergy1Key = "myAllergy1"
    val myAllergy2Key = "myAllergy2"
    val myAllergy3Key = "myAllergy3"
    val myAllergy4Key = "myAllergy4"
    val myAllergy5Key = "myAllergy5"
    val myAllergy6Key = "myAllergy6"
    val myAllergy7Key = "myAllergy7"
    val myAllergy8Key = "myAllergy8"
    val myAllergy9Key = "myAllergy9"
    val myAllergy10Key = "myAllergy10"
    val myAllergy11Key = "myAllergy11"
    val myAllergy12Key = "myAllergy12"
    val myAllergy13Key = "myAllergy13"
    val myAllergy14Key = "myAllergy14"
    val myAllergy15Key = "myAllergy15"
    val myAllergy16Key = "myAllergy16"
    val myAllergy17Key = "myAllergy17"
    val myAllergy18Key = "myAllergy18"


    fun getDiffTimeText(targetTime: Long): String {
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes

        if(diffDay == 0) {
            if(diffHours == 0 && diffMinutes == 0) {
                return "방금 전"
            }

            return if(diffHours > 0) {
                "" + diffHours + "시간 전"
            } else {
                "" + diffMinutes + "분 전"
            }
        } else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime))
        }
    }

    fun toggleButton(view: View) {
        if(view.visibility == View.VISIBLE) {
            view.visibility = View.INVISIBLE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}