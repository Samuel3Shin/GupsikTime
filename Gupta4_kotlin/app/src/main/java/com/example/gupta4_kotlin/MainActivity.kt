package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.gupta4_kotlin.databinding.CalendarHeaderBinding
import com.example.gupta4_kotlin.databinding.CalendarDayBinding
import com.example.gupta4_kotlin.databinding.ActivityMainBinding
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

    var serviceUrl: String = ""
    var serviceKey: String = ""

    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    var district_code = "J10"
    var school_code = "7530184"

    var strUrl = ""
    var result = ""

    var breakfastTextViewList: MutableList<TextView> = mutableListOf()
    var lunchTextViewList: MutableList<TextView> = mutableListOf()
    var dinnerTextViewList: MutableList<TextView> = mutableListOf()

    var breakfastFamilyViewList: MutableList<View> = mutableListOf()
    var lunchFamilyViewList: MutableList<View> = mutableListOf()
    var dinnerFamilyViewList: MutableList<View> = mutableListOf()

    val allergyKeyList: MutableList<String> = mutableListOf()
    val allergyDayList: MutableList<LocalDate> = mutableListOf()


    var date_code = ""

    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //배너 광고 추가
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        serviceUrl = getString(R.string.neis_api_service_url)
        serviceKey = getString(R.string.neis_api_service_key)

        schoolName.setText(preference.getString(Utils.schoolNameKey, ""))

        // shared preference에서 교육청 코드와 학교 코드 불러오는데, 만약에 없으면 그냥 위의 default값(안산동산고등학교 코드) 내뱉음.
        district_code = preference.getString("districtCode", district_code).toString()
        school_code = preference.getString("schoolCode", school_code).toString()

        var childCnt: Int = gupsikInfoGroup.getChildCount()
        for(i in 0 until childCnt) {
            var viewId = gupsikInfoGroup.getChildAt(i).getResources().getResourceEntryName(gupsikInfoGroup.getChildAt(i).id).toString()
            if(viewId.startsWith("breakfastTextView_")) {
                breakfastTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            } else if(viewId.startsWith("lunchTextView_")) {
                lunchTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            } else if(viewId.startsWith("dinnerTextView_")) {
                dinnerTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            }  else if(viewId.startsWith("breakfast_family_")) {
                breakfastFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
            } else if(viewId.startsWith("lunch_family_")) {
                lunchFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
            } else if(viewId.startsWith("dinner_family_")) {
                dinnerFamilyViewList.add(gupsikInfoGroup.getChildAt(i))
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
                binding.calendarView.scrollToDate(today)
            }

            // 다른 달에서 오늘 버튼 눌렀을 때 현재 날짜 하이라이트 사라지는 이슈
            Handler(Looper.getMainLooper()).postDelayed({
                selectDate(today)
            }, 100)
        }

        var tmpDate = ""
        highlighterButton.setOnClickListener {
            Utils.toggleButton(highlighterButton)
            Utils.toggleButton(highlighterImageView)
            Utils.toggleButton(highlighterPressedButton)
            Utils.toggleButton(highlighterPressedImageView)

            var alphaValue = 0.3F
            calendar_box.alpha = alphaValue
            calendarView.alpha = alphaValue
            todayButton.alpha = alphaValue
            todayPressedButton.alpha = alphaValue
            todayTextView.alpha = alphaValue
            shareButton.alpha = alphaValue
            shareImageView.alpha = alphaValue

            tmpDate = dateTextView.text.toString()
            dateTextView.setText("좋아하는 메뉴를 눌러 하이라이트!")
            dateTextView.setTextColor(getResources().getColor(R.color.windowBlue))
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

            dateTextView.setText(tmpDate)
            dateTextView.setTextColor(getResources().getColor(R.color.black))
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
                    textView.makeVisible()
                    when (day.date) {

                        selectedDate -> {
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.background = null
                        }
                    }

                    for(i in 0 until allergyDayList.size) {
                        if (day.date.equals(allergyDayList.get(i))){
                            textView.setTextColorRes(R.color.allergy)
                        }
                    }

                } else {
                    textView.makeInVisible()
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            var monthNum = month.month.toString()
            if(monthNum.get(0) == '0') {
                monthNum = monthNum.get(1).toString()
            }
            val title = "${monthNum}월"

            binding.exFiveMonthYearText.text = title

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
            }
        }

        previousMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previous)
                selectedDate?.let {
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
                selectedDate?.let {
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

        var highlightedTextViews = ""
        for(i in 0 until breakfastTextViewList.size) {
            breakfastTextViewList.get(i).setOnClickListener {it as TextView

                if(highlighterPressedButton.visibility == View.VISIBLE) {
                    highlightedTextViews = preference.getString(date_code + "breakfast", "").toString()
                    // 클릭했을 때,

                    if(highlightedTextViews!!.indexOf(i.toString()+",", 0) != -1) {
                        it.setBackgroundResource(android.R.color.transparent)
                        highlightedTextViews = highlightedTextViews.replace(i.toString() +",", "")
                        preference.edit().putString(date_code + "breakfast", highlightedTextViews).apply()

                    } else {
                        highlightedTextViews = highlightedTextViews + i.toString() + ","
                        preference.edit().putString(date_code + "breakfast", highlightedTextViews).apply()
                        it.background = getResources().getDrawable(R.drawable.highlight)

                    }
                }
            }
        }

        for(i in 0 until lunchTextViewList.size) {
            lunchTextViewList.get(i).setOnClickListener {it as TextView

                if(highlighterPressedButton.visibility == View.VISIBLE) {
                    highlightedTextViews = preference.getString(date_code + "lunch", "").toString()

                    if(highlightedTextViews!!.indexOf(i.toString()+",", 0) != -1) {
                        it.setBackgroundResource(android.R.color.transparent)
                        highlightedTextViews = highlightedTextViews.replace(i.toString() +",", "")
                        preference.edit().putString(date_code + "lunch", highlightedTextViews).apply()

                    } else {
                        highlightedTextViews = highlightedTextViews + i.toString() + ","
                        preference.edit().putString(date_code + "lunch", highlightedTextViews).apply()
                        it.background = getResources().getDrawable(R.drawable.highlight)

                    }
                }
            }
        }

        for(i in 0 until dinnerTextViewList.size) {
            dinnerTextViewList.get(i).setOnClickListener {it as TextView

                if(highlighterPressedButton.visibility == View.VISIBLE) {
                    highlightedTextViews = preference.getString(date_code + "dinner", "").toString()

                    if(highlightedTextViews!!.indexOf(i.toString()+",", 0) != -1) {
                        it.setBackgroundResource(android.R.color.transparent)
                        highlightedTextViews = highlightedTextViews.replace(i.toString() +",", "")
                        preference.edit().putString(date_code + "dinner", highlightedTextViews).apply()

                    } else {
                        highlightedTextViews = highlightedTextViews + i.toString() + ","
                        preference.edit().putString(date_code + "dinner", highlightedTextViews).apply()
                        it.background = getResources().getDrawable(R.drawable.highlight)

                    }
                }
            }
            // Charlie : listener 등록하는 것도 같은 코드가 반복되는데 setClickListenerWithTextViewList(textViewList) 정도로 만들어서 쓰면 좋을 것 같아!!
        }


    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo -> {
                return true
            }

            R.id.menu_board -> {
                val intent = Intent(this, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
                return true
            }

            R.id.menu_myPage -> {
                val intent = Intent(this, MyPostsActivity::class.java)
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

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
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

            date_code = date.toString().replace("-", "")
            showMealInfo(date_code)
            // Charlie : date 파싱할 필요 없이 이미 LocalDate 클래스에서 구현된 접근자 쓰면 될 것 같아!
            // date.dayOfMonth / date.monthValue / date.year
            var date_lst: MutableList<String> = date.toString().split("-") as MutableList<String>
            if(date_lst.get(1).get(0) == '0') {
                date_lst.set(1, date_lst.get(1).get(1).toString())
            }

            if(date_lst.get(2).get(0) == '0') {
                date_lst.set(2, date_lst.get(2).get(1).toString())
            }

//            dateTextView.setText(date_lst.get(0) + "년 " + date_lst.get(1) + "월 " + date_lst.get(2) + "일")
            dateTextView.setText(date_lst.get(1) + "월 " + date_lst.get(2) + "일")

        }
    }

    private fun showMealInfo(date_code: String) {
        strUrl = "$serviceUrl?KEY=$serviceKey&ATPT_OFCDC_SC_CODE=$district_code&SD_SCHUL_CODE=$school_code&MMEAL_SC_CODE=&MLSV_YMD=$date_code"
        run()
    }

    val client = OkHttpClient()

    fun run() {
        val request = Request.Builder()
            .url(strUrl)
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

                    var resStr = response.body!!.string()

                    result = resStr

                    runOnUiThread {
                        parse(result)
                        binding.calendarView.notifyCalendarChanged()    // Update calendar with meal info
                    }
                }
            }
        })
    }

    fun parse(result: String) {
        var school = ""
        var date = ""
        var bld = ""
        var dish = ""
        var meal_school = false
        var meal_date = false
        var meal_bld = false
        var meal_dish = false
        var isBreakfastExist = false
        var isLunchExist = false
        var isDinnerExist = false

//        schoolName.setText("")

        val factory = XmlPullParserFactory.newInstance()
        val xmlpp = factory.newPullParser()

        xmlpp.setInput(StringReader(result))

        var eventType = xmlpp.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
            } else if (eventType == XmlPullParser.START_TAG) {
                val tag_name = xmlpp.name
                when (tag_name) {
                    "SCHUL_NM" -> meal_school = true
                    "MLSV_YMD" -> meal_date = true
                    "MMEAL_SC_NM" -> meal_bld = true
                    "DDISH_NM" -> meal_dish = true
                }
            } else if (eventType == XmlPullParser.TEXT) {
                if (meal_school) {
                    school = xmlpp.text
//                    schoolName.setText(school + "\n")
                    meal_school = false
                }
                if (meal_date) {
                    date = xmlpp.text
//                    dateTextView.setText(date)
                    meal_date = false
                }
                if (meal_bld) {
                    bld = xmlpp.text
                    meal_bld = false
                }
                if (meal_dish) {
                    dish = xmlpp.text
                    meal_dish = false
                    // Charlie : 대박.. 요거 response json이나 정리된건 없었어? <br/>로 구분된거 파싱했다니!
                    dish = dish.replace("*", "")
                    var dishList: List<String> = dish.split("<br/>")
                    var tmpTextViewList: MutableList<TextView> = mutableListOf()
                    var whichMeal: String = ""
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

                    var isAllergyDay = false;
                    var highlightedTextViews = preference.getString(date_code + whichMeal, "")
                    // TODO :: Charlie : 요거는 급식메뉴 중간에 변경되면 다른 급식메뉴가 하이라이트 되는 버그 나올 수도 있겠다!
                    for(i in 0 until dishList.size)  {
                        // 하이라이트 저장된 거 불러오기
                        if(highlightedTextViews!!.indexOf(i.toString()+",", 0) != -1) {
                            tmpTextViewList.get(i).background = getResources().getDrawable(R.drawable.highlight)
                        } else {
                            tmpTextViewList.get(i).setBackgroundResource(android.R.color.transparent)
                        }

                        // View.visibility 초기화
                        tmpTextViewList.get(i).visibility = View.VISIBLE

                        //View 글자색 초기화
                        tmpTextViewList.get(i).setTextColor(Color.parseColor("#000000"))

                        var str = dishList.get(i)
                        var allergyNumList = str.split(".")
                        var firstOne = allergyNumList.get(0)
                        var menuName: String = ""
                        var firstAllergyInfo = ""

                        var digitsInFirstone = ""
                        // 숫자 있는지부터 확인
                        if(firstOne.length >= 2 && firstOne.get(firstOne.length - 1) != ')') {
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

                        var isAllergyFlag = false

                        if(firstAllergyInfo != "" && firstAllergyInfo.toInt() > 0 && firstAllergyInfo.toInt() <= 18 && preference.getInt(
                                allergyKeyList.get(firstAllergyInfo.toInt()), 0) != 0) {
                            isAllergyFlag = true
                        }

                        if(!isAllergyFlag) {
                            for(i in 1 until allergyNumList.size) {
                                if(allergyNumList.get(i) == "" || allergyNumList.get(i).toInt() > 18 || allergyNumList.get(i).toInt() < 0) continue
                                if(preference.getInt(allergyKeyList.get(allergyNumList.get(i).toInt()), 0) != 0) {
                                    isAllergyFlag = true
                                    break
                                }
                            }
                        }


                        if(isAllergyFlag) {
                            tmpTextViewList.get(i).setText(menuName)
                            tmpTextViewList.get(i).setTextColor(Color.parseColor("#FF7FFF"))
                            isAllergyDay = true;
                        } else {
                            tmpTextViewList.get(i).setText(menuName)
                        }

                    }

                    if(isAllergyDay){
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        allergyDayList.add(LocalDate.parse(date, dateTimeFormatter))
                    }

                    for(i in dishList.size until 12) {
                        tmpTextViewList.get(i).visibility = View.GONE
                    }

                }
            } else if (eventType == XmlPullParser.END_TAG) {

            }
            eventType = xmlpp.next()
        }

        if(!isBreakfastExist) {
            for(i in 0 until breakfastTextViewList.size) {
                breakfastTextViewList.get(i).makeGone()
            }

            for(i in 0 until breakfastFamilyViewList.size) {
                breakfastFamilyViewList.get(i).makeGone()
            }

        } else {
            for(i in 0 until breakfastFamilyViewList.size) {
                breakfastFamilyViewList.get(i).makeVisible()
            }
        }

        if(!isLunchExist) {
            for(i in 0 until lunchTextViewList.size) {
                lunchTextViewList.get(i).makeGone()
            }

            for(i in 0 until lunchFamilyViewList.size) {
                lunchFamilyViewList.get(i).makeGone()
            }
        } else {
            for(i in 0 until lunchFamilyViewList.size) {
                lunchFamilyViewList.get(i).makeVisible()
            }
        }

        if(!isDinnerExist) {
            for(i in 0 until dinnerTextViewList.size) {
                dinnerTextViewList.get(i).makeGone()
            }

            for(i in 0 until dinnerFamilyViewList.size) {
                dinnerFamilyViewList.get(i).makeGone()
            }
        } else {
            for(i in 0 until dinnerFamilyViewList.size) {
                dinnerFamilyViewList.get(i).makeVisible()
            }
        }

    }


    // Share callback function
    private fun onClickShareButton(){
        gupsikInfoGroup.isDrawingCacheEnabled = true
        gupsikInfoGroup.buildDrawingCache()

//        val bitmap = gupsikInfoGroup.getDrawingCache()
//            testImageView.setImageBitmap(bitmap)

        val bitmap: Bitmap = Bitmap.createBitmap(gupsikInfoGroup.measuredWidth, gupsikInfoGroup.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas: Canvas = Canvas(bitmap)

        gupsikInfoGroup.draw(canvas)

        if (bitmap == null)
            return

        var bitmapURI = Utils.getImageUri(this@MainActivity, bitmap)
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
