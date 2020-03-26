package com.tgfc.tw.controller;

import com.tgfc.tw.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/statistics")
public class StatisticsController {

    @Autowired
    StatisticsService statisticsService;

    /**
     * 取得User的特休天數
     */
    @GetMapping(value = "/allUserAnnualLeave")
    public void getAllUserAnnualLeave() {
        statisticsService.getAllUserAnnualLeave();
    }

    /**
     * 取得排班人員(早、中、晚班)的例、休假天數
     */
    @GetMapping(value = "/getShiftPersonnelCausalLeaveAndDayOff")
    public void getShiftPersonnelCausalLeaveAndDayOff() {
        statisticsService.getShiftPersonnelCausalLeaveAndDayOff();
    }

}
