package com.gooselab.gupta4_kotlin

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    const val isSetSchoolKey = "isSetSchool"
    const val districtCodeKey = "districtCode"
    const val schoolCodeKey = "schoolCode"
    const val schoolNameKey = "schoolName"
    const val myPostIdsKey = "myPostIds"
    const val myLikedPostIdsKey = "myLikedPostIds"
    const val isAllergyInfoInsertedKey = "isAllergyInfoInserted"
    const val breakfastHighlightKey = "breakfastHighlightId"
    const val lunchHighlightKey = "lunchHighlightId"
    const val dinnerHighlightKey = "dinnerHighlightId"
    const val myAllergy1Key = "myAllergy1"
    const val myAllergy2Key = "myAllergy2"
    const val myAllergy3Key = "myAllergy3"
    const val myAllergy4Key = "myAllergy4"
    const val myAllergy5Key = "myAllergy5"
    const val myAllergy6Key = "myAllergy6"
    const val myAllergy7Key = "myAllergy7"
    const val myAllergy8Key = "myAllergy8"
    const val myAllergy9Key = "myAllergy9"
    const val myAllergy10Key = "myAllergy10"
    const val myAllergy11Key = "myAllergy11"
    const val myAllergy12Key = "myAllergy12"
    const val myAllergy13Key = "myAllergy13"
    const val myAllergy14Key = "myAllergy14"
    const val myAllergy15Key = "myAllergy15"
    const val myAllergy16Key = "myAllergy16"
    const val myAllergy17Key = "myAllergy17"
    const val myAllergy18Key = "myAllergy18"


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

    fun getImageUri(inContext: Context, inImage: Bitmap, title: String): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, title, null)
        return Uri.parse(path)
    }

    fun isDigit(str: String): Boolean {
        return str.matches("^[0-9]+$".toRegex())
    }

}