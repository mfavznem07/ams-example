package com.tgfc.tw.service;

import com.tgfc.tw.service.exception.TimestampErrorException;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

public interface TimeConvertService {

    Timestamp getEndTime(String date) throws TimestampErrorException;

    Timestamp getFirstDateTimeOfYear();

    long setDate(String year, String month, String day) throws TimestampErrorException;

    Date getStartDate(String date) throws TimestampErrorException;

    Date getEndDate(Date startDate, int month);

    java.sql.Date getSqlDate(java.util.Date date); // java.util.Date 轉 java.sql.Date

    int getCalendarYear(java.util.Date date);

    int getCalendarMonth(java.util.Date date);

    int getCalendarDay(java.util.Date date);

    Timestamp calendarToTimestamp(Calendar calendar);

    Timestamp stringToTimestamp(int year, int month, int day);

    Calendar timeStampToCalendar(Timestamp startTime);
}
