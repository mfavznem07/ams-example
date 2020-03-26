package com.tgfc.tw.service;

import com.tgfc.tw.entity.po.Statistics;
import com.tgfc.tw.entity.po.User;
import com.tgfc.tw.service.exception.TimestampErrorException;
import com.tgfc.tw.service.exception.statistics.StatisticsNotExistsExcption;

import java.sql.Timestamp;

public interface StatisticsService {

    int getCausalLeaveDaysOfMonth(String date) throws TimestampErrorException;

    int getDayOffDaysOfMonth(String date, int causalLeaveDays) throws TimestampErrorException;

    void addStatistics(User user, Statistics.LeaveType leaveType, Timestamp startTime, Timestamp endTime, int hours);

    void updateStatisticsHours(int id, int hours) throws StatisticsNotExistsExcption;

    void getAllUserAnnualLeave();

    void getShiftPersonnelCausalLeaveAndDayOff();
}
