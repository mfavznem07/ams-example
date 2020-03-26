package com.tgfc.tw.task;

import com.tgfc.tw.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GetStatisticsTask {

    @Autowired
    StatisticsService statisticsService;

    /* 排程設定排班人員的例、休假天數 */
    @Scheduled(cron = "0 0 0 1/1 1/1 ?") // 每天00:00:00執行
//    @Scheduled(cron = "*/5 * * * * ?") // 每5秒一個週期，測試用
    public void setShiftPersonnelCausalLeaveAndDayOff() {
        statisticsService.getShiftPersonnelCausalLeaveAndDayOff();
    }

    /* 排程新增或更新User的特天數 */
    @Scheduled(cron = "0 0 0 1/1 1/1 ?") // 每天00:00:00執行
//    @Scheduled(cron = "*/5 * * * * ?") // 每5秒一個週期，測試用
    public void setAllUserAnnualLeave() {
        statisticsService.getAllUserAnnualLeave();
    }
}
