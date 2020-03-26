package com.tgfc.tw.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tgfc.tw.entity.po.Holiday;
import com.tgfc.tw.model.Records;
import com.tgfc.tw.repository.HolidayRepository;
import com.tgfc.tw.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    HolidayRepository holidayRepository;

    @Override
    public void insertHoliday(List<Records> recordsList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        for (Records records : recordsList) {
            try {
                Long time = sdf.parse(records.getDate()).getTime();
                Optional<Holiday> holiday = holidayRepository.findByDate(sdf.parse(records.getDate()));
                if (holiday.isPresent()) {
                    holiday.get().setDate(new Date(time));
                    holiday.get().setName(records.getName());
                    holiday.get().setIsHoliday(records.getIsHoliday());
                    holiday.get().setHolidayCategory(records.getHolidayCategory());
                    holiday.get().setDescription(records.getDescription());
                    holidayRepository.save(holiday.get());
                } else {
                    Holiday holiday1 = new Holiday();
                    holiday1.setDate(new Date(time));
                    holiday1.setName(records.getName());
                    holiday1.setIsHoliday(records.getIsHoliday());
                    holiday1.setHolidayCategory(records.getHolidayCategory());
                    holiday1.setDescription(records.getDescription());
                    holidayRepository.save(holiday1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getHoliday() {
        BufferedReader br;
        String line;
        StringBuilder sb = new StringBuilder();
        HttpURLConnection http;
        try {
            /* Start of Fix */
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            } };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /* End of the fix*/

            URL url = new URL("https://data.ntpc.gov.tw/api/v1/rest/datastore/382000000A-000077-002");
            http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setDoInput(true);
            http.setDoOutput(true);

            br = new BufferedReader(new InputStreamReader(http.getInputStream()));
            while ((line = br.readLine()) != null) {
                if(!line.contains("//")){
                    sb.append(line);
                }
            }
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            com.tgfc.tw.model.Holiday holiday = gson.fromJson(jsonObject, com.tgfc.tw.model.Holiday.class);
            this.insertHoliday(holiday.getResult().getRecords());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
