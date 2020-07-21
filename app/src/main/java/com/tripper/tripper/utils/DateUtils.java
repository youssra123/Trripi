package com.tripper.tripper.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import com.tripper.tripper.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class DateUtils {

    private static final SimpleDateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);

    public static String databaseDateToString(Date date){
        return databaseDateFormat.format(date);
    }

    public static Date databaseStringToDate(String dateString){
       return stringToDate(dateString, databaseDateFormat);
    }

    public static Date stringToDate(String dateString, SimpleDateFormat dateFormat){
        Date date = null;
        try {
            date = dateFormat.parse(dateString);
        }catch (ParseException e){
            e.getCause();
        }catch (Exception e) {
            date = getDateOfToday();
        }
        return date;
    }

    public static Date getDateOfToday(){
        return Calendar.getInstance().getTime();
    }

    public static SimpleDateFormat getFormDateFormat(){
        return new SimpleDateFormat("E, MMM dd, yyyy", getDeviceLocale());
    }

    public static SimpleDateFormat getFormDateTimeFormat(){
        return new SimpleDateFormat("E, MMM dd, yyyy    HH:mm", getDeviceLocale());
    }

    public static SimpleDateFormat getLandmarkHeaderDateFormat(){
        return new SimpleDateFormat("dd/MM/yyyy EEEE", getDeviceLocale());
    }

    public static SimpleDateFormat getLandmarkTimeDateFormat(){
        return new SimpleDateFormat("HH:mm", getDeviceLocale());
    }
    public static SimpleDateFormat getTripListDateFormat(){
        return new SimpleDateFormat("dd/MM/yyyy",getDeviceLocale());
    }

    public static SimpleDateFormat getImageTimeStampDateFormat(){
        return new SimpleDateFormat("yyyyMMdd_HHmmss", getDeviceLocale());
    }

    private static Locale getDeviceLocale(){
        return Locale.getDefault();
    }

    public static DatePickerDialog getDatePicker(Context context, Date currentDate, DatePickerDialog.OnDateSetListener listener) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.datePickerTheme, listener, 0, 0, 0);
        updateDatePicker(datePickerDialog, currentDate);
        return datePickerDialog;
    }

    public static void updateDatePicker(DatePickerDialog datePickerDialog, Date currentDate){
        Calendar newCalendar = new GregorianCalendar();
        newCalendar.setTime(currentDate);
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog.updateDate(currentYear, currentMonth, currentDay);
    }

    public static TimePickerDialog getTimePicker(Context context, Date currentDate, TimePickerDialog.OnTimeSetListener listener) {

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, R.style.datePickerTheme, listener, 0, 0, true);
        updateTimePicker(timePickerDialog, currentDate);
        return timePickerDialog;
    }

    public static void updateTimePicker(TimePickerDialog timePickerDialog, Date currentDate){
        Calendar newCalendar = new GregorianCalendar();
        newCalendar.setTime(currentDate);
        int currentHour = newCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = newCalendar.get(Calendar.MINUTE);
        timePickerDialog.updateTime(currentHour, currentMinute);
    }


    public static boolean isSameDay(Date date1, Date date2) {
        return compareDates(date1, date2) == 0;
    }

    public static boolean isFirstLaterThanSecond(Date date1, Date date2) {
        return compareDates(date1, date2) > 0;
    }

    private static int compareDates(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return fmt.format(date1).compareTo(fmt.format(date2));
    }
}
