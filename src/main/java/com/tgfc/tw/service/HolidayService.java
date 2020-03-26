package com.tgfc.tw.service;

import com.tgfc.tw.model.Records;

import java.util.List;

public interface HolidayService {

    void insertHoliday(List<Records> recordsList);

    void getHoliday();
}
