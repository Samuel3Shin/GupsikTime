package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.gupta4_kotlin.databinding.CalendarHeaderBinding
import com.example.gupta4_kotlin.databinding.CalendarDayBinding
import com.example.gupta4_kotlin.databinding.ActivityMainBinding
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    var serviceUrl: String = "https://open.neis.go.kr/hub/mealServiceDietInfo"
    var serviceKey: String = "ce674eea5a53470680157d24c26d07a4"

    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    var district_code = "J10"
    var school_code = "7530184"

    var strUrl = ""
    var result = ""

    var breakfastTextViewList: MutableList<TextView> = mutableListOf()
    var lunchTextViewList: MutableList<TextView> = mutableListOf()
    var dinnerTextViewList: MutableList<TextView> = mutableListOf()
    val allergyKeyList: MutableList<String> = mutableListOf()


    private lateinit var binding: ActivityMainBinding
    private val today = LocalDate.now()
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // shared preference에서 교육청 코드와 학교 코드 불러오는데, 만약에 없으면 그냥 위의 default값(안산동산고등학교 코드) 내뱉음.
        district_code = preference.getString("districtCode", district_code).toString()
        school_code = preference.getString("schoolCode", school_code).toString()

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

        todayButton.setOnClickListener {
            Utils.toggleButton(todayPressedButton)
            Utils.toggleButton(todayButton)

            selectDate(today)
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
                        today -> {
                            textView.setTextColorRes(R.color.allergy)
//                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                        }
                        selectedDate -> {
                            textView.setTextColorRes(R.color.allergy)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.black)
                            textView.background = null
                        }
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        binding.calendarView.monthScrollListener = {
//            homeActivityToolbar.title = if (it.year == today.year) {
//                titleSameYearFormatter.format(it.yearMonth)
//            } else {
//                titleFormatter.format(it.yearMonth)
//            }

            // Select the first day of the month when
            // we scroll to a new month.
//            selectDate(it.yearMonth.atDay(1))
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
                        tv.setTextColorRes(R.color.black)
                    }
                }
            }
        }

        var childCnt: Int = gupsikInfoGroup.getChildCount()
        for(i in 0 until childCnt) {
            var viewId = gupsikInfoGroup.getChildAt(i).getResources().getResourceEntryName(
                gupsikInfoGroup.getChildAt(
                    i
                ).id
            ).toString()
            if(viewId.startsWith("breakfastTextView_")) {
                breakfastTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            } else if(viewId.startsWith("lunchTextView_")) {
                lunchTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            } else if(viewId.startsWith("dinnerTextView_")) {
                dinnerTextViewList.add(gupsikInfoGroup.getChildAt(i) as TextView)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo -> {
                Toast.makeText(this@MainActivity, "급식메뉴!", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.menu_board -> {
                Toast.makeText(this@MainActivity, "게시판!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                return true
            }

            R.id.menu_myPage -> {
                Toast.makeText(this@MainActivity, "마이 페이지!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MySettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
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

            var date_code = date.toString().replace("-", "")
            showMealInfo(date_code)
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

        schoolName.setText("")

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
                    schoolName.setText(school + "\n")
                    meal_school = false
                }
                if (meal_date) {
                    date = xmlpp.text
                    dateTextView.setText(date)
                    meal_date = false
                }
                if (meal_bld) {
                    bld = xmlpp.text
                    meal_bld = false
                }
                if (meal_dish) {
                    dish = xmlpp.text
                    meal_dish = false

                    var dishList: List<String> = dish.split("<br/>")
                    var tmpTextViewList: MutableList<TextView> = mutableListOf()
                    when(bld) {
                        "조식" -> {
                            tmpTextViewList = breakfastTextViewList
                        }
                        "중식" -> {
                            tmpTextViewList = lunchTextViewList
                        }
                        "석식" -> {
                            tmpTextViewList = dinnerTextViewList
                        }
                    }

                    for(i in 0 until dishList.size)  {
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
                        } else {
                            tmpTextViewList.get(i).setText(menuName)
                        }

                    }

                    for(i in dishList.size until 12) {
                        tmpTextViewList.get(i).visibility = View.GONE
                    }

                }
            } else if (eventType == XmlPullParser.END_TAG) {

            }
            eventType = xmlpp.next()
        }

    }

}
