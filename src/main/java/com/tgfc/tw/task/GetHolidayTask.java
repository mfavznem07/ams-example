package com.tgfc.tw.task;

import com.tgfc.tw.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GetHolidayTask {

    @Autowired
    HolidayService holidayService;

    @Scheduled(cron = "0 0 0 1/31 * ?")//每月最靠近1號的第一個工作日為周期
//    @Scheduled(cron = "*/20 * * * * ?")//每20秒一個週期，測試用
    public void holidayTask() {
        holidayService.getHoliday();
    }
}
