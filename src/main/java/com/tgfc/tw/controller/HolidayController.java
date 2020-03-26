package com.tgfc.tw.controller;

import com.tgfc.tw.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/holiday")
public class HolidayController {

    @Autowired
    HolidayService holidayService;

    /* 取得行政院公告的例假日、國定假日、補班日、彈性上班日 */
    @GetMapping(value = "/getHoliday")
    public void getHoliday() {
        holidayService.getHoliday();
    }
}
