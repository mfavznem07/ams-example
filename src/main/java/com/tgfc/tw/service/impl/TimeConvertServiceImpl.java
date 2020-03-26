package com.tgfc.tw.service.impl;

import com.tgfc.tw.service.TimeConvertService;
import com.tgfc.tw.service.exception.TimestampErrorException;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class TimeConvertServiceImpl implements TimeConvertService {

    private final static String JANUARY = "01";
    private final static String FIRST = "01";
    private final static int ONE_MONTH = 1;

    /**
     * 取得每月最後一天的 23:59:59.999
     *
     * @param date (yyyy-MM)
     */
    @Override
    public Timestamp getEndTime(String date) throws TimestampErrorException {
        Date startDate;
        Date endDate;
        try {
            startDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(getStartDate(date).toString()).getTime());
            endDate = getEndDate(startDate, ONE_MONTH);
        } catch (ParseException e) {
            throw new TimestampErrorException("日期格式錯誤(yyyy-MM)!");
        }
        String endTime = endDate.toString() + " " + "23:59:59.999";
        return Timestamp.valueOf(endTime);
    }

    /**
     * 每年的1月1日 00:00:00.0
     *
     * @return
     */
    @Override
    public Timestamp getFirstDateTimeOfYear() {
        String firstDateTimeOfYear = Calendar.getInstance().get(Calendar.YEAR) + "-" + JANUARY + "-" + FIRST + " " + "00:00:00.0";
        return Timestamp.valueOf(firstDateTimeOfYear);
    }

    /**
     * @param year
     * @param month
     * @param day
     * @return
     * @throws TimestampErrorException
     */
    @Override
    public long setDate(String year, String month, String day) throws TimestampErrorException {
        String date = year + "-" + month + "-" + day;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date dt;
        try {
            dt = sdf.parse(date);
        } catch (ParseException e) {
            throw new TimestampErrorException("日期格式錯誤(yyyy-MM)!");
        }
        return dt.getTime();
    }

    /**
     * 取得某月第一天
     *
     * @param date (yyyy-MM)
     */
    @Override
    public Date getStartDate(String date) throws TimestampErrorException {
        Date startDate;
        try {
            startDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(date + "-" + FIRST).getTime());
        } catch (ParseException e) {
            throw new TimestampErrorException("日期格式錯誤(yyyy-MM)!");
        }
        return startDate;
    }

    /**
     * 取得某月最後一天
     *
     * @param startDate
     * @param month
     * @return
     */
    @Override
    public Date getEndDate(Date startDate, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        return new java.sql.Date(calendar.getTime().getTime()); // endDate
    }

    /**
     * java.util.Date 轉 java.sql.Date
     *
     * @param date
     * @return
     */
    @Override
    public java.sql.Date getSqlDate(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }

    @Override
    public int getCalendarYear(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public int getCalendarMonth(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    @Override
    public int getCalendarDay(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public Timestamp calendarToTimestamp(Calendar calendar) {
        return new Timestamp(calendar.getTimeInMillis());
    }

    @Override
    public Timestamp stringToTimestamp(int year, int month, int day) {
        return Timestamp.valueOf(year + "-" + month + "-" + day + " " + "00:00:00.00");
    }

    @Override
    public Calendar timeStampToCalendar(Timestamp startTime) {
        java.util.Date date = new java.util.Date(startTime.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}