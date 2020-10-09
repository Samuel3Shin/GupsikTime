@file:Suppress("NAME_SHADOWING")
package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.gooselab.gupta4_kotlin.databinding.CalendarHeaderBinding
import com.gooselab.gupta4_kotlin.databinding.CalendarDayBinding
import com.gooselab.gupta4_kotlin.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private var serviceUrl: String = ""
    private var serviceKey: String = ""
    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    private var districtCode = "J10"
    private var schoolCode = "7530184"

    var result = ""

    private var breakfastTextViewList: MutableList<TextView> = mutableListOf()
    private var lunchTextViewList: MutableList<TextView> = mutableListOf()
    private var dinnerTextViewList: MutableList<TextView> = mutableListOf()

    private var breakfastFamilyViewList: MutableList<View> = mutableListOf()
    private var lunchFamilyViewList: MutableList<View> = mutableListOf()
    private var dinnerFamilyViewList: MutableList<View> = mutableListOf()

    private val allergyKeyList: MutableList<String> = mutableListOf()
    private val allergyDayList: MutableList<LocalDate> = mutableListOf()

    private var dateCode = ""

    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //배너 광고 추가 (원래는 this 였는데, applicationContext로 바꿨다.)
        MobileAds.initialize(applicationContext, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        serviceUrl = getString(R.string.neis_api_service_url)
        serviceKey = getString(R.string.neis_api_service_key)

        schoolName.text = preference.getString(Utils.schoolNameKey, "")

        // shared preference에서 교육청 코드와 학교 코드 불러오는데, 만약에 없으면 그냥 위의 default값(안산동산고등학교 코드) 내뱉음.
        districtCode = preference.getString("districtCode", districtCode).toString()
        schoolCode = preference.getString("schoolCode", schoolCode).toString()

        val childCnt: Int = gupsikInfoGroup.childCount
        for(i in 0 until childCnt) {
            val viewId = gupsikInfoGroup.getChildAt(i).resources.getResourceEntryName(gupsikInfoGroup.getChildAt(i).id).toString()
            when {
                viewId.startsWith("breakfastTextView_") -> {
                    breakfastTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
                }
                viewId.startsWith("lunchTextView_") -> {
                    lunchTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
                }
                viewId.startsWith("dinnerTextView_") -> {
                    dinnerTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
                }
                viewId.startsWith("breakfast_family_") -> {
                    breakfastFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
                }
                viewId.startsWith("lunch_family_") -> {
                    lunchFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
                }
                viewId.startsWith("dinner_family_") -> {
                    dinnerFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
                }
            }
        }

        allergyKeyList.addAll(
            listOf(
                "",
                Utils.myAllergy1Key,
                Utils.myAllergy2Key,
                Utils.myAllergy3Key,
                Utils.myAllergy4Key,
                Utils.myAllergy5Key,
                Utils.myAllergy6Key,
                Utils.myAllergy7Key,
                Utils.myAllergy8Key,
                Utils.myAllergy9Key,
                Utils.myAllergy10Key,
                Utils.myAllergy11Key,
                Utils.myAllergy12Key,
                Utils.myAllergy13Key,
                Utils.myAllergy14Key,
                Utils.myAllergy15Key,
                Utils.myAllergy16Key,
                Utils.myAllergy17Key,
                Utils.myAllergy18Key
            )
        )

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@MainActivity, it)
            popup.setOnMenuItemClickListener(this@MainActivity)
            popup.inflate(R.menu.main)
            popup.show()
        }

        shareButton.setOnClickListener {
            onClickShareButton()
        }

        todayButton.setOnClickListener {
            Utils.toggleButton(todayPressedButton)
            Utils.toggleButton(todayButton)

            binding.calendarView.findFirstVisibleMonth()?.let {
              binding.calendarView.smoothScrollToMonth(YearMonth.now())
            }

            // 다른 달에서 오늘 버튼 눌렀을 때 현재 날짜 하이라이트 사라지는 이슈
            Handler(Looper.getMainLooper()).postDelayed({
                selectDate(today)
            }, 200)
        }

        var tmpDate = ""
        highlighterButton.setOnClickListener {
            Utils.toggleButton(highlighterButton)
            Utils.toggleButton(highlighterImageView)
            Utils.toggleButton(highlighterPressedButton)
            Utils.toggleButton(highlighterPressedImageView)

            val alphaValue = 0.3F
            calendar_box.alpha = alphaValue
            calendarView.alpha = alphaValue
            todayButton.alpha = alphaValue
            todayPressedButton.alpha = alphaValue
            todayTextView.alpha = alphaValue
            shareButton.alpha = alphaValue
            shareImageView.alpha = alphaValue

            tmpDate = dateTextView.text.toString()
            dateTextView.text = "좋아하는 메뉴를 눌러 하이라이트!"
            dateTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.windowBlue))
            schoolName.makeGone()
        }

        highlighterPressedButton.setOnClickListener {
            Utils.toggleButton(highlighterButton)
            Utils.toggleButton(highlighterImageView)
            Utils.toggleButton(highlighterPressedButton)
            Utils.toggleButton(highlighterPressedImageView)

            calendar_box.alpha = 1F
            calendarView.alpha = 1F
            todayButton.alpha = 1F
            todayPressedButton.alpha = 1F
            todayTextView.alpha = 1F
            shareButton.alpha = 1F
            shareImageView.alpha = 1F
            // Charlie : 토글과 alpha 조절하는 건 함수만들어서 코드 라인 수 줄이면 좋을 것 같아!

            dateTextView.text = tmpDate
            dateTextView.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
            schoolName.makeVisible()
        }

        // Customized Calendar
        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()

        binding.calendarView.apply {
            setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
            scrollToMonth(currentMonth)
        }

        if (savedInstanceState == null) {
            binding.calendarView.post {
                // init monthly meal info
                showMealInfoWithMonth(currentMonth)

                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.dayText

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {

                    // init textview
                    textView.makeVisible()
                    textView.setTextColorRes(R.color.black)
                    textView.background = null

                    if (isAllergyDay(day.date)) {
                        textView.setTextColorRes(R.color.allergy)
                    }

                    if (isHighlightDay(day.date)) {
                        highlightString(textView)
//                        textView.setBackgroundResource(R.drawable.highlight)
                    }

                    if (selectedDate == day.date) {
                        textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                    }

                } else {
                    textView.makeInVisible()
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            var monthNum = month.month.toString()
            if(monthNum[0] == '0') {
                monthNum = monthNum[1].toString()
            }
            val title = "${monthNum}월"

            binding.exFiveMonthYearText.text = title


            showMealInfoWithMonth(month.yearMonth)

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
            }
        }

        previousMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previous)
                selectedDate?.let {it ->
                    // Clear selection if we scroll to a new month.
                    selectedDate = null
                    binding.calendarView.notifyDateChanged(it)
                }
                binding.calendarView.notifyMonthChanged(it.yearMonth.previous)
            }
        }

        nextMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.next)
                selectedDate?.let {it ->
                    // Clear selection if we scroll to a new month.
                    selectedDate = null
                    binding.calendarView.notifyDateChanged(it)
                }
                binding.calendarView.notifyMonthChanged(it.yearMonth.next)
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.let {
                            when(it) {
                                "MONDAY" -> "월"
                                "TUESDAY" -> "화"
                                "WEDNESDAY" -> "수"
                                "THURSDAY" -> "목"
                                "FRIDAY" -> "금"
                                "SATURDAY" -> "토"
                                "SUNDAY" -> "일"
                                else -> ""
                            }
                        }
                        tv.setTextColorRes(R.color.white)
                    }
                }
            }
        }

        setClickListenerWithTextViewList(breakfastTextViewList, "breakfast")
        setClickListenerWithTextViewList(lunchTextViewList, "lunch")
        setClickListenerWithTextViewList(dinnerTextViewList, "dinner")

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo -> {
                return true
            }

            R.id.menu_board -> {
                val intent = Intent(applicationContext, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_myPage -> {
                val intent = Intent(applicationContext, MyPostsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

    private fun updateAdapterForDate(date: LocalDate) {
//        binding.exThreeSelectedDateText.text = selectionFormatter.format(date)
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
            binding.calendarView.notifyDateChanged(date)
            updateAdapterForDate(date)

            if(date != today) {
                todayButton.visibility = View.VISIBLE
                todayPressedButton.visibility = View.INVISIBLE
            } else {
                todayButton.visibility = View.INVISIBLE
                todayPressedButton.visibility = View.VISIBLE
            }

            dateCode = date.toString().replace("-", "")
            showMealInfo(dateCode)
            dateTextView.text = "${date.monthValue}월 ${date.dayOfMonth}일"

        }
    }

    private fun showMealInfo(dateCode: String) {
        val strUrl = "$serviceUrl?KEY=$serviceKey&ATPT_OFCDC_SC_CODE=$districtCode&SD_SCHUL_CODE=$schoolCode&MMEAL_SC_CODE=&MLSV_YMD=$dateCode"
        run(strUrl, false)
    }

    private fun isAlreadyCalledMonthlyMealInfo(yearMonth: YearMonth): Boolean {
        for(i in 0 until allergyDayList.size) {
            val allergyDay : LocalDate = allergyDayList[i]
            if (allergyDay.year == yearMonth.year && allergyDay.month == yearMonth.month)
                return true
        }

        return false
    }

    private fun showMealInfoWithMonth(yearMonth: YearMonth){

        if (isAlreadyCalledMonthlyMealInfo(yearMonth))
            return

        val monthCode = yearMonth.toString().replace("-", "")

        val strUrl = "$serviceUrl?KEY=$serviceKey&ATPT_OFCDC_SC_CODE=$districtCode&SD_SCHUL_CODE=$schoolCode&MMEAL_SC_CODE=&MLSV_YMD=$monthCode"
        run(strUrl, true)
    }

    private val client = OkHttpClient()

    private fun run(targetUrl: String, isMonthRefresh:Boolean) {
        val request = Request.Builder()
            .url(targetUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }

                    val resStr = response.body!!.string()

                    result = resStr

                    runOnUiThread {
                        if(isMonthRefresh) {
                            parseForMonth(result)
                        } else {
                            parse(result)
                        }
                        binding.calendarView.notifyCalendarChanged()    // Update calendar with meal info
                    }
                }
            }
        })
    }

    fun parseForMonth(result: String) {
        var date = ""
        var dish: String
        var isMealDateExist = false
        var isMealDishExist = false

        val factory = XmlPullParserFactory.newInstance()
        val xmlpp = factory.newPullParser()

        xmlpp.setInput(StringReader(result))

        var eventType = xmlpp.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                when (xmlpp.name) {
                    "MLSV_YMD" -> isMealDateExist = true
                    "DDISH_NM" -> isMealDishExist = true
                }
            } else if (eventType == XmlPullParser.TEXT) {

                if (isMealDateExist) {
                    date = xmlpp.text
                    isMealDateExist = false
                }

                if (isMealDishExist) {
                    dish = xmlpp.text
                    isMealDishExist = false
                    val dishList: List<String> = dish.split("<br/>")
                    var isAllergyDay = false

                    for(i in dishList.indices)  {

                        val str = dishList[i]
                        val allergyNumList = str.split(".")
                        val firstOne = allergyNumList[0]
                        var firstAllergyInfo = ""
                        var digitsInFirstone = ""

                        // 숫자 있는지부터 확인
                        if(firstOne.length >= 2 && firstOne.get(firstOne.length - 1) != ')') {
                            digitsInFirstone = firstOne.substring(firstOne.length - 2, firstOne.length).filter{it.isDigit()}
                        }

                        if(digitsInFirstone != "") {
                            firstAllergyInfo = digitsInFirstone
                        }

                        if(firstAllergyInfo != "" && firstAllergyInfo.toInt() > 0 && firstAllergyInfo.toInt() <= 18 && preference.getInt(
                                allergyKeyList[firstAllergyInfo.toInt()], 0) != 0) {
                            isAllergyDay = true
                        }

                        if(!isAllergyDay) {
                            for(i in 1 until allergyNumList.size) {
                                if(allergyNumList[i] == "" || !Utils.isDigit(allergyNumList[i]) || allergyNumList[i].toInt() > 18 || allergyNumList[i].toInt() < 0) continue
                                if(preference.getInt(allergyKeyList[allergyNumList[i].toInt()], 0) != 0) {
                                    isAllergyDay = true
                                    break
                                }
                            }
                        }
                    }

                    if(isAllergyDay){
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val allergyDay = LocalDate.parse(date, dateTimeFormatter)
                        if (!allergyDayList.contains(allergyDay))
                            allergyDayList.add(LocalDate.parse(date, dateTimeFormatter))
                    }

                }
            } else if (eventType == XmlPullParser.END_TAG) {

            }
            eventType = xmlpp.next()
        }
    }

    fun parse(result: String) {
        var school = ""
        var date = ""
        var bld = ""
        var dish: String
        var isSchoolExist = false
        var isMealDateExist = false
        var isMealBldExist = false
        var isMealDishExist = false
        var isBreakfastExist = false
        var isLunchExist = false
        var isDinnerExist = false

        val factory = XmlPullParserFactory.newInstance()
        val xmlpp = factory.newPullParser()

        xmlpp.setInput(StringReader(result))

        var eventType = xmlpp.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                when (xmlpp.name) {
                    "SCHUL_NM" -> isSchoolExist = true
                    "MLSV_YMD" -> isMealDateExist = true
                    "MMEAL_SC_NM" -> isMealBldExist = true
                    "DDISH_NM" -> isMealDishExist = true
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (isSchoolExist) {
                    school = xmlpp.text
//                    schoolName.setText(school + "\n")
                    isSchoolExist = false
                }
                if (isMealDateExist) {
                    date = xmlpp.text
//                    dateTextView.setText(date)
                    isMealDateExist = false
                }
                if (isMealBldExist) {
                    bld = xmlpp.text
                    isMealBldExist = false
                }
                if (isMealDishExist) {
                    dish = xmlpp.text
                    isMealDishExist = false
                    val dishList: List<String> = dish.split("<br/>")
                    var tmpTextViewList: MutableList<TextView> = mutableListOf()
                    var whichMeal = ""
                    when(bld) {
                        "조식" -> {
                            tmpTextViewList = breakfastTextViewList
                            whichMeal = "breakfast"
                            isBreakfastExist = true
                        }
                        "중식" -> {
                            tmpTextViewList = lunchTextViewList
                            whichMeal = "lunch"
                            isLunchExist = true
                        }
                        "석식" -> {
                            tmpTextViewList = dinnerTextViewList
                            whichMeal = "dinner"
                            isDinnerExist = true
                        }
                    }

                    val highlightedTextViews = preference.getString(dateCode + whichMeal, "")
                    // TODO :: Charlie : 요거는 급식메뉴 중간에 변경되면 다른 급식메뉴가 하이라이트 되는 버그 나올 수도 있겠다!
                    for(i in dishList.indices)  {

                        // View.visibility 초기화
                        tmpTextViewList[i].visibility = View.VISIBLE

                        //View 글자색 초기화
                        tmpTextViewList[i].setTextColor(Color.parseColor("#000000"))

                        val str = dishList[i]
                        val allergyNumList = str.split(".")
                        val firstOne = allergyNumList[0]
                        var menuName = ""
                        var firstAllergyInfo = ""

                        var digitsInFirstone = ""
                        // 숫자 있는지부터 확인
                        if(firstOne.length >= 2 && firstOne[firstOne.length - 1] != ')') {
                            digitsInFirstone = firstOne.substring(firstOne.length - 2, firstOne.length).filter{it.isDigit()}
                        }

                        if(digitsInFirstone != "") {
                            firstAllergyInfo = digitsInFirstone
                            if(digitsInFirstone.length == 1) {
                                menuName = firstOne.substring(0, firstOne.length-1)
                            } else if(digitsInFirstone.length == 2) {
                                menuName = firstOne.substring(0, firstOne.length-2)
                            }
                        } else {
                            menuName = firstOne
                        }

                        menuName = menuName.trim()

                        // 메뉴 이름 양 끝에 있는 특수문자 삭제. 하지만 시작하는 부분의 '(' 와 끝 부분의 ')'는 남겨둔다.
                        if(menuName[0].toInt() < 127 && menuName[0].toInt() > 32 && menuName[0].toInt() != 40) {
                            menuName = menuName.substring(1, menuName.length)
                        }

                        if(menuName[menuName.length-1].toInt() < 127 && menuName[menuName.length-1].toInt() > 32 && menuName[menuName.length-1].toInt() != 41) {
                            menuName = menuName.substring(0, menuName.length-1)
                        }

                        var isAllergyFlag = false

                        if(firstAllergyInfo != "" && firstAllergyInfo.toInt() > 0 && firstAllergyInfo.toInt() <= 18 && preference.getInt(
                                allergyKeyList[firstAllergyInfo.toInt()], 0) != 0) {
                            isAllergyFlag = true
                        }

                        if(!isAllergyFlag) {
                            for(i in 1 until allergyNumList.size) {
                                if(allergyNumList[i] == "" || !Utils.isDigit(allergyNumList[i]) || allergyNumList[i].toInt() > 18 || allergyNumList[i].toInt() < 0) continue
                                if(preference.getInt(allergyKeyList[allergyNumList[i].toInt()], 0) != 0) {
                                    isAllergyFlag = true
                                    break
                                }
                            }
                        }

                        if(isAllergyFlag) {
                            tmpTextViewList[i].text = menuName
                            tmpTextViewList[i].setTextColor(ContextCompat.getColor(applicationContext, R.color.allergy))
                        } else {
                            tmpTextViewList[i].text = menuName
                        }

                        if(highlightedTextViews!!.indexOf("$i,", 0) != -1) {
                            highlightString(tmpTextViewList[i])
                        }

                    }

                    for(i in dishList.size until 12) {
                        tmpTextViewList[i].visibility = View.GONE
                    }

                }
            } else if (eventType == XmlPullParser.END_TAG) {

            }
            eventType = xmlpp.next()
        }

        if(!isBreakfastExist) {
            for(i in 0 until breakfastTextViewList.size) {
                breakfastTextViewList[i].makeGone()
            }

            for(i in 0 until breakfastFamilyViewList.size) {
                breakfastFamilyViewList[i].makeGone()
            }

        } else {
            for(i in 0 until breakfastFamilyViewList.size) {
                breakfastFamilyViewList[i].makeVisible()
            }
        }

        if(!isLunchExist) {
            for(i in 0 until lunchTextViewList.size) {
                lunchTextViewList[i].makeGone()
            }

            for(i in 0 until lunchFamilyViewList.size) {
                lunchFamilyViewList[i].makeGone()
            }
        } else {
            for(i in 0 until lunchFamilyViewList.size) {
                lunchFamilyViewList[i].makeVisible()
            }
        }

        if(!isDinnerExist) {
            for(i in 0 until dinnerTextViewList.size) {
                dinnerTextViewList[i].makeGone()
            }

            for(i in 0 until dinnerFamilyViewList.size) {
                dinnerFamilyViewList[i].makeGone()
            }
        } else {
            for(i in 0 until dinnerFamilyViewList.size) {
                dinnerFamilyViewList[i].makeVisible()
            }
        }

        if(!(isBreakfastExist || isLunchExist || isDinnerExist)) {
            breakfast_family_1.makeVisible()
            breakfast_family_2.makeVisible()
            breakfast_family_2.text = "오늘자 급식 정보가 없습니다 ^^;"
            breakfast_family_3.makeVisible()

            last_line.makeInVisible()
        } else {
            breakfast_family_2.text = "아침식사"
            last_line.makeVisible()
        }

    }

    // check whether the day is allergic or highlight
    private fun isAllergyDay(date: LocalDate): Boolean{
        if(allergyDayList.contains(date)) return true
        return false
    }

    private fun isHighlightDay(date: LocalDate): Boolean{
        val tempDateCode = date.toString().replace("-", "")
        if(preference.getString(tempDateCode + "breakfast", "")!!.isNotEmpty())
            return true
        if(preference.getString(tempDateCode + "lunch", "")!!.isNotEmpty())
            return true
        if(preference.getString(tempDateCode + "dinner", "")!!.isNotEmpty())
            return true

        return false
    }


    // Share callback function
    private fun onClickShareButton(){
        val bitmap: Bitmap = Bitmap.createBitmap(gupsikInfoGroup.measuredWidth, gupsikInfoGroup.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        gupsikInfoGroup.draw(canvas)

        if (bitmap == null)
            return

        val bitmapURI = Utils.getImageUri(applicationContext, bitmap, "급식정보")
        //TODO: facebook은 text intent를 허용하지 않는듯?? 앱다운로드 링크를 어떻게 보낼지 생각해봐야함
        //TODO: 사진만 보내는 것은 잘 되는데, 텍스트도 같이 보내는 건 안 될때가 있다. 왜 그런지 살펴봐야함.
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, bitmapURI)
            putExtra(Intent.EXTRA_TEXT, "이것은 공유링크")
        }
        startActivity(Intent.createChooser(shareIntent, "share image and text!"))
    }

    private fun highlightString(mTextView: TextView) {

        //Get the text from text view and create a spannable string
        val spannableString = SpannableString(mTextView.text)
        //Get the previous spans and remove them
        val backgroundSpans = spannableString.getSpans(
            0, spannableString.length,
            BackgroundColorSpan::class.java
        )
        for (span in backgroundSpans) {
            spannableString.removeSpan(span)
        }

        spannableString.setSpan(BackgroundColorSpan(ContextCompat.getColor(applicationContext, R.color.highlight)), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //Set the final text on TextView
        mTextView.text = spannableString
    }

    private fun setClickListenerWithTextViewList(textViewList: MutableList<TextView>, whichMeal: String) {
        var highlightedTextViews: String
        for(i in 0 until textViewList.size) {
            textViewList[i].setOnClickListener {it as TextView

                if(highlighterPressedButton.visibility == View.VISIBLE) {
                    highlightedTextViews = preference.getString(dateCode + whichMeal, "").toString()

                    if(highlightedTextViews.indexOf("$i,", 0) != -1) {

                        it.text = it.text.toString()
                        highlightedTextViews = highlightedTextViews.replace("$i,", "")
                        preference.edit().putString(dateCode + whichMeal, highlightedTextViews).apply()

                    } else {
                        highlightedTextViews = "$highlightedTextViews$i,"
                        preference.edit().putString(dateCode + whichMeal, highlightedTextViews).apply()

                        highlightString(it)

                    }
                }
            }
        }
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
