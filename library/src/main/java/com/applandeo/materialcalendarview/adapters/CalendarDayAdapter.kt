package com.applandeo.materialcalendarview.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.annimon.stream.Stream
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.R
import com.applandeo.materialcalendarview.utils.CalendarProperties
import com.applandeo.materialcalendarview.utils.DateUtils
import com.applandeo.materialcalendarview.utils.DayColorsUtils
import com.applandeo.materialcalendarview.utils.ImageUtils
import com.applandeo.materialcalendarview.utils.SelectedDay

import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * This class is responsible for loading a one day cell.
 *
 *
 * Created by Mateusz Kornakiewicz on 24.05.2017.
 */

internal class CalendarDayAdapter(private val mCalendarPageAdapter: CalendarPageAdapter, context: Context, private val mCalendarProperties: CalendarProperties,
                                  dates: ArrayList<Date>, pageMonth: Int) : ArrayAdapter<Date>(context, mCalendarProperties.itemLayoutResource, dates) {
    private val mLayoutInflater: LayoutInflater
    private val mPageMonth: Int
    private val mToday = DateUtils.getCalendar()

    init {
        mPageMonth = if (pageMonth < 0) 11 else pageMonth
        mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null) {
            view = mLayoutInflater.inflate(mCalendarProperties.itemLayoutResource, parent, false)
        }

        val dayLabel = view!!.findViewById(R.id.dayLabel) as TextView
        val dayIcon = view.findViewById(R.id.dayIcon) as ImageView
        val dayIcon2 = view.findViewById(R.id.dayIcon2) as ImageView
        val dayIcon3 = view.findViewById(R.id.dayIcon3) as ImageView

        val day = GregorianCalendar()
        day.time = getItem(position)

        // Loading an image of the event
        loadIcon(listOf(dayIcon, dayIcon2, dayIcon3), day)

        setLabelColors(dayLabel, day)

        dayLabel.text = day.get(Calendar.DAY_OF_MONTH).toString()
        return view
    }

    private fun setLabelColors(dayLabel: TextView, day: Calendar) {
        // Setting not current month day color
        if (!isCurrentMonthDay(day)) {
            DayColorsUtils.setDayColors(dayLabel, mCalendarProperties.anotherMonthsDaysLabelsColor,
                    Typeface.NORMAL, R.drawable.background_transparent)
            return
        }

        // Set view for all SelectedDays
        if (isSelectedDay(day)) {
            Stream.of(mCalendarPageAdapter.selectedDays)
                    .filter { selectedDay -> selectedDay.calendar == day }
                    .findFirst().ifPresent { selectedDay -> selectedDay.view = dayLabel }

            DayColorsUtils.setSelectedDayColors(dayLabel, mCalendarProperties)
            return
        }

        // Setting disabled days color
        if (!isActiveDay(day)) {
            DayColorsUtils.setDayColors(dayLabel, mCalendarProperties.disabledDaysLabelsColor,
                    Typeface.NORMAL, R.drawable.background_transparent)
            return
        }

        // Setting current month day color
        DayColorsUtils.setCurrentMonthDayColors(day, mToday, dayLabel, mCalendarProperties)
    }

    private fun isSelectedDay(day: Calendar): Boolean {
        return (mCalendarProperties.calendarType != CalendarView.CLASSIC && day.get(Calendar.MONTH) == mPageMonth
                && mCalendarPageAdapter.selectedDays.contains(SelectedDay(day)))
    }

    private fun isCurrentMonthDay(day: Calendar): Boolean {
        return day.get(Calendar.MONTH) == mPageMonth && !(mCalendarProperties.minimumDate != null && day.before(mCalendarProperties.minimumDate) || mCalendarProperties.maximumDate != null && day.after(mCalendarProperties.maximumDate))
    }

    private fun isActiveDay(day: Calendar): Boolean {
        return !mCalendarProperties.disabledDays.contains(day)
    }

    private fun loadIcon(dayIcons: List<ImageView>, day: Calendar) {
        if (mCalendarProperties.eventDays == null || !mCalendarProperties.eventsEnabled) {
            dayIcons.forEach { it.visibility = View.GONE }
            return
        }

        Stream.of<EventDay>(mCalendarProperties.eventDays).filter { eventDate -> eventDate.calendar == day }.forEachIndexed { index, eventDay ->
            if(dayIcons.count() > index) {
                val icon = dayIcons[index]
                ImageUtils.loadImage(icon, eventDay.imageDrawable)
                icon.visibility = View.VISIBLE
                // If a day doesn't belong to current month then image is transparent
                if (!isCurrentMonthDay(day) || !isActiveDay(day)) {
                    icon.alpha = 0.12f
                }
            }
        }
    }
}
