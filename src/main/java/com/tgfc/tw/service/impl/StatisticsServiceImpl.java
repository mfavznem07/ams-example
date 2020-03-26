package com.tgfc.tw.service.impl;

import com.tgfc.tw.entity.po.Holiday;
import com.tgfc.tw.entity.po.Statistics;
import com.tgfc.tw.entity.po.User;
import com.tgfc.tw.entity.response.statistics.AddAnnualLeaveResponse;
import com.tgfc.tw.repository.HolidayRepository;
import com.tgfc.tw.repository.StatisticsRepository;
import com.tgfc.tw.repository.UserRepository;
import com.tgfc.tw.service.StatisticsService;
import com.tgfc.tw.service.TimeConvertService;
import com.tgfc.tw.service.exception.TimestampErrorException;
import com.tgfc.tw.service.exception.statistics.StatisticsNotExistsExcption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TimeConvertService timeConvertService;

    @Autowired
    StatisticsRepository statisticsRepository;

    @Autowired
    HolidayRepository holidayRepository;

    private final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private final static int ZERO_DAY = 0;
    private final static int ONE_DAY = 1;
    private final static int TWO_DAYS = 2;
    private final static int THREE_DAYS = 3;
    private final static int SEVEN_DAYS = 7;
    private final static int TEN_DAYS = 10;
    private final static int FOURTEEN_DAYS = 14;
    private final static int FIFTEEN_DAYS = 15;
    private final static int SIXTEEN_DAYS = 16;
    private final static int MAXIMUM_ANNUAL_LEAVE_DAYS = 30;

    private final static int THREE_MONTH = 3;
    private final static int SIX_MONTH = 6;
    private final static int TWELVE_MONTH = 12;

    private final static int ONE_YEAR = 1;
    private final static int TWO_YEARS = 2;
    private final static int THREE_YEARS = 3;
    private final static int FOUR_YEARS = 4;
    private final static int FIVE_YEARS = 5;
    private final static int SIX_YEARS = 6;
    private final static int SEVEN_YEARS = 7;
    private final static int EIGHT_YEARS = 8;
    private final static int NINE_YEARS = 9;
    private final static int TEN_YEARS = 10;
    private final static int THE_SAME_YEAR = 0;

    private final static int EIGHT_HOURS = 8;

    private final static int SUNDAY = 1;

    private final static int ONE_MONTH = 1;

    @Override
    public void addStatistics(User user, Statistics.LeaveType leaveType, Timestamp startTime, Timestamp endTime, int hours) {
        Statistics statistics = new Statistics();
        statistics.setUser(user);
        statistics.setLeaveType(leaveType);
        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        statistics.setHours(hours);
        statisticsRepository.save(statistics);
    }

    /**
     * 計算某月星期日天數即為例假天數
     *
     * @param date (yyyy-MM-dd)
     */
    @Override
    public int getCausalLeaveDaysOfMonth(String date) throws TimestampErrorException {
        int causalLeaveDays = 0; //例假天數
        try {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(Timestamp.valueOf(date + " " + "00:00:00.0"));
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            sdf1.setLenient(false);
//            SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE");
            int maximumDays = calendar1.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = calendar1.get(Calendar.DAY_OF_MONTH); i <= maximumDays; i++) {
                int year = calendar1.get(Calendar.YEAR);
                int month = calendar1.get(Calendar.MONTH) + 1;
                java.util.Date dt = sdf1.parse(year + "-" + month + "-" + i);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTime(dt);
                if (calendar2.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
                    causalLeaveDays++;
//                    System.out.println(sdf1.format(dt) + " " + sdf2.format(dt));
                }
            }
        } catch (ParseException e) {
            throw new TimestampErrorException("日期格式錯誤(yyyy-MM)!");
        }
        return causalLeaveDays;
    }

    /**
     * 依開始日期計算某月的休假天數(假日 - 例假)
     *
     * @param date            (yyyy-MM-dd)
     * @param causalLeaveDays (例假天數)
     */
    @Override
    public int getDayOffDaysOfMonth(String date, int causalLeaveDays) {
        Date startDate = timeConvertService.getSqlDate(Timestamp.valueOf(date + " " + "00:00:00.0"));
        Date endDate = timeConvertService.getEndDate(startDate, ONE_MONTH);
        List<Holiday> holidays = holidayRepository.findAllByDateBetween(startDate, endDate);
        holidays = holidays.stream().filter(holiday -> holiday.getIsHoliday().contains("是")).collect(Collectors.toList());
        int dayOffDays = holidays.size() - causalLeaveDays;
        return dayOffDays;
    }

    @Override
    @Transactional
    public void updateStatisticsHours(int id, int hours) throws StatisticsNotExistsExcption {
        Statistics statistics = checkStatisticsIdExists(id);
        statistics.setHours(hours);
    }

    private Statistics checkStatisticsIdExists(int id) throws StatisticsNotExistsExcption {
        Optional<Statistics> statisticsOptional = statisticsRepository.findById(id);
        if (!statisticsOptional.isPresent()) {
            throw new StatisticsNotExistsExcption("您所查詢的休假統計資料不存在!");
        }
        return statisticsOptional.get();
    }

    /* 建立、更新所有User的特休 */
    @Override
    public void getAllUserAnnualLeave() {
        /*
         * 3 檢查休假統計表是否有資料
         * 3-1 是：不動作
         * 3-2 否：檢查到職日期，如果當前時間減去到職日期
         * 3-2-1 滿「3」個月，未滿「6」個月：特休「2」日
         * 3-2-2 滿「6」個月，未滿「1」年：特休「3」日
         * 3-2-3 滿「1」年，未滿「2」年：特休「7」日
         * 3-2-4 滿「2」年，未滿「3」年：特休「10」日
         * 3-2-5 滿「3」年，未滿「5」年：特休「14」日
         * 3-2-6 滿「5」年，未滿「10」年：特休「15」日
         * 3-2-7 滿「10」年以上，每「1」年加「1」日，加至「30」日為止
         * 滿「10」年，未滿「11」年：特休「16」日
         * 滿「11」年，未滿「12」年：特休「17」日
         * 滿「12」年，未滿「13」年：特休「18」日
         * 滿「13」年，未滿「14」年：特休「19」日
         * 滿「14」年，未滿「15」年：特休「20」日
         * .
         * .
         * .
         * 滿「24」年，未滿「25」年：特休「30」日
         * 滿「25」年，未滿「26」年：特休「30」日
         */
        List<User> allUsers = userRepository.findAll();

        // 取得Calendar的當前時間
        Calendar currentTime = Calendar.getInstance();

//        List<User> allUsers = new ArrayList<>(); // 測試用
//        User u1 = userRepository.getByAccount("tgfc033"); // 測試用
//        User u2 = userRepository.getByAccount("tgfc035"); // 測試用
//        User u3 = userRepository.getByAccount("tgfc037"); // 測試用
//        User u4 = userRepository.getByAccount("tgfc039"); // 測試用
//        User u5 = userRepository.getByAccount("tgfc040"); // 測試用
//        allUsers.add(u1); // 測試用
//        allUsers.add(u2); // 測試用
//        allUsers.add(u3); // 測試用
//        allUsers.add(u4); // 測試用
//        allUsers.add(u5); // 測試用
//        Timestamp testTime = Timestamp.valueOf("2030-04-13 00:00:00.0"); // 測試用
//        currentTime.setTime(testTime); // 測試用

        for (User user : allUsers) {
            // 將Calendar的當前時間格式轉成Timestamp
            Timestamp time = timeConvertService.calendarToTimestamp(currentTime);

            // 檢查User當前時間的特休是否存在
            Optional<Statistics> annualLeaveStatisticsOptional = statisticsRepository.findUserAnnualLeave(user, Statistics.LeaveType.ANNUAL_LEAVE, time);

            // 不存在->新增
            if (!annualLeaveStatisticsOptional.isPresent()) {
                // 取得User當前時間點的特休天數及起迄時間
                AddAnnualLeaveResponse response = this.getAnnualLeaveDays(user, currentTime);

                // 取得特休天數
                int annualLeaveDays = response.getAnnualLeaveDays();

                // 滿3個月後，就有特休天數2天
                if (annualLeaveDays != ZERO_DAY) {
                    Timestamp startTime = response.getStartTime();
                    Timestamp endTime = response.getEndTime();
                    // 新增當前時間之前的特休
                    this.addPreviousAnnualLeave(user, startTime, annualLeaveDays);

                    // 新增當前時間的特休
                    this.addStatistics(user, Statistics.LeaveType.ANNUAL_LEAVE, startTime, endTime, annualLeaveDays * EIGHT_HOURS);
                }
            }
        }
    }

    private AddAnnualLeaveResponse getAnnualLeaveDays(User user, Calendar calendar) {
        AddAnnualLeaveResponse response = new AddAnnualLeaveResponse();
        // 當前月曆時間的年、月、日
        int calendarOfYear = calendar.get(Calendar.YEAR);
        int calendarOfMonth = calendar.get(Calendar.MONTH) + 1;
        int calendarOfDay = calendar.get(Calendar.DAY_OF_MONTH);

        // 入職時間的年、月、日，轉成Calendar格式
        Calendar entryDate = Calendar.getInstance();
        entryDate.setTime(user.getEntryDate());
        int entryDateOfYear = entryDate.get(Calendar.YEAR);
        int entryDateOfMonth = entryDate.get(Calendar.MONTH) + 1;
        int entryDateOfDay = entryDate.get(Calendar.DAY_OF_MONTH);

        // 特休的起、迄時間
        Timestamp startTime = null;
        Timestamp endTime = null;

        // 當前年份=入職年份
        if ((calendarOfYear - entryDateOfYear) == THE_SAME_YEAR) {
            // 入職月份滿3個月，檢查日
            if ((calendarOfMonth - entryDateOfMonth) == THREE_MONTH) {
                // 入職未滿3個月，無特休
                if (calendarOfDay < entryDateOfDay) {
                    return response;
                } else { // 入職剛滿或超過3個月，特休2日
                    response.setAnnualLeaveDays(TWO_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                    response.setStartTime(startTime);
                    response.setEndTime(endTime);
                }
            } else if ((calendarOfMonth - entryDateOfMonth) > THREE_MONTH && (calendarOfMonth - entryDateOfMonth) < SIX_MONTH) { // 入職超過3個月，未滿6個月，特休2日
                response.setAnnualLeaveDays(TWO_DAYS);
                startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                response.setStartTime(startTime);
                endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                response.setEndTime(endTime);
            } else if ((calendarOfMonth - entryDateOfMonth) == SIX_MONTH) { // 入職月份滿6個月，檢查日
                // 入職未滿6個月，特休2日
                if (calendarOfDay < entryDateOfDay) {
                    response.setAnnualLeaveDays(TWO_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                } else { // 入職剛滿或超過6個月，特休3日
                    response.setAnnualLeaveDays(THREE_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, SIX_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, SIX_MONTH);
                }
                response.setStartTime(startTime);
                response.setEndTime(endTime);
            } else if ((calendarOfMonth - entryDateOfMonth) > SIX_MONTH) { // 入職月份超過6個月，未滿1年，特休3日
                response.setAnnualLeaveDays(THREE_DAYS);
                startTime = this.getStartTimeForAddMonth(entryDate, SIX_MONTH);
                response.setStartTime(startTime);
                endTime = this.getEndTimeForAddMonth(entryDate, SIX_MONTH);
                response.setEndTime(endTime);
            }
        } else if ((calendarOfYear - entryDateOfYear) == ONE_YEAR) {
            int tmpMonth = calendarOfMonth + ONE_YEAR * 12;
            // 入職月份滿3個月，檢查日
            if ((tmpMonth - entryDateOfMonth) == THREE_MONTH) {
                // 入職超過3個月，特休2日
                if (calendarOfDay > entryDateOfDay) {
                    response.setAnnualLeaveDays(TWO_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                } else if (calendarOfDay == entryDateOfDay) { // 入職剛滿3個月，特休2日
                    response.setAnnualLeaveDays(TWO_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                }
                response.setStartTime(startTime);
                response.setEndTime(endTime);
            } else if ((tmpMonth - entryDateOfMonth) > THREE_MONTH && (tmpMonth - entryDateOfMonth) < SIX_MONTH) { // 入職超過3個月，未滿6個月，特休2日
                response.setAnnualLeaveDays(TWO_DAYS);
                startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                response.setStartTime(startTime);
                endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                response.setEndTime(endTime);
            } else if ((tmpMonth - entryDateOfMonth) == SIX_MONTH) { // 入職月份滿6個月，檢查日
                // 入職未滿6個月，特休2日
                if (calendarOfDay < entryDateOfDay) {
                    response.setAnnualLeaveDays(TWO_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, THREE_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, THREE_MONTH);
                } else { // 入職剛滿或超過6個月，特休3日
                    response.setAnnualLeaveDays(THREE_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, SIX_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, SIX_MONTH);
                }
                response.setStartTime(startTime);
                response.setEndTime(endTime);
            } else if ((tmpMonth - entryDateOfMonth) > SIX_MONTH && (tmpMonth - entryDateOfMonth) < TWELVE_MONTH) { // 入職月份超過6個月，未滿12個月特休3日
                response.setAnnualLeaveDays(THREE_DAYS);
                startTime = this.getStartTimeForAddMonth(entryDate, SIX_MONTH);
                response.setStartTime(startTime);
                endTime = this.getEndTimeForAddMonth(entryDate, SIX_MONTH);
                response.setEndTime(endTime);
            } else if ((tmpMonth - entryDateOfMonth) == ONE_YEAR * 12) { // 入職月份滿12個月，檢查日
                // 入職未滿12個月，特休3日
                if (calendarOfDay < entryDateOfDay) {
                    response.setAnnualLeaveDays(THREE_DAYS);
                    startTime = this.getStartTimeForAddMonth(entryDate, SIX_MONTH);
                    endTime = this.getEndTimeForAddMonth(entryDate, SIX_MONTH);
                } else { // 入職剛滿或超過1年，特休7日
                    response.setAnnualLeaveDays(SEVEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, ONE_YEAR);
                    endTime = this.getEndTime(startTime);
                }
                response.setStartTime(startTime);
                response.setEndTime(endTime);
            } else if ((tmpMonth - entryDateOfMonth) > ONE_YEAR * 12) {  // 入職超過1年，未滿2年，特休7日
                response.setAnnualLeaveDays(SEVEN_DAYS);
                startTime = this.getStartTimeForAddYear(entryDate, ONE_YEAR);
                response.setStartTime(startTime);
                endTime = this.getEndTime(startTime);
                response.setEndTime(new Timestamp(endTime.getTime() - 1));
            }
        } else if ((calendarOfYear - entryDateOfYear) == TWO_YEARS) {
            int tmpMonth = calendarOfMonth + TWO_YEARS * 12;
            // 入職超過1年，未滿2年，特休7日
            if ((tmpMonth - entryDateOfMonth) < TWO_YEARS * 12) {
                response.setAnnualLeaveDays(SEVEN_DAYS);
                startTime = this.getStartTimeForAddYear(entryDate, ONE_YEAR);
            } else if ((tmpMonth - entryDateOfMonth) == TWO_YEARS * 12) { // 入職月份滿2年，檢查日
                // 入職超過1年，未滿2年，特休7日
                if (calendarOfDay < entryDateOfDay) {
                    response.setAnnualLeaveDays(SEVEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, ONE_YEAR);
                } else { // 入職剛滿或超過2年，特休10日
                    response.setAnnualLeaveDays(TEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, TWO_YEARS);
                }
            } else { // 入職超過2年，特休10日
                response.setAnnualLeaveDays(TEN_DAYS);
                startTime = this.getStartTimeForAddYear(entryDate, TWO_YEARS);
            }
            response.setStartTime(startTime);
            endTime = getEndTime(startTime);
            response.setEndTime(endTime);
        } else if ((calendarOfYear - entryDateOfYear) > TWO_YEARS && (calendarOfYear - entryDateOfYear) < FIVE_YEARS) {
            // 入職年份滿3年
            if ((calendarOfYear - entryDateOfYear) == THREE_YEARS) {
                int tmpMonth = calendarOfMonth + THREE_YEARS * 12;
                // 入職超過2年，未滿3年，特休10日
                if ((tmpMonth - entryDateOfMonth) < THREE_YEARS * 12) {
                    response.setAnnualLeaveDays(TEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, TWO_YEARS);
                } else if ((tmpMonth - entryDateOfMonth) == THREE_YEARS * 12) { // 入職月份滿3年，檢查日
                    // 入職超過2年，未滿3年，特休10日
                    if (calendarOfDay < entryDateOfDay) {
                        response.setAnnualLeaveDays(TEN_DAYS);
                        startTime = this.getStartTimeForAddYear(entryDate, TWO_YEARS);
                    } else { // 入職剛滿或超過3年，特休14日
                        response.setAnnualLeaveDays(FOURTEEN_DAYS);
                        startTime = this.getStartTimeForAddYear(entryDate, THREE_YEARS);
                    }
                } else { // 入職超過3年，特休14日
                    response.setAnnualLeaveDays(FOURTEEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, THREE_YEARS);
                }
                response.setStartTime(startTime);
                endTime = getEndTime(startTime);
                response.setEndTime(endTime);
            } else { // 入職年份超過3年，未滿5年(包含3年以上未滿4年、4年以上未滿5年)，特休14日
                if (calendarOfMonth < entryDateOfMonth) {
                    startTime = this.getStartTimeForAddYear(entryDate, THREE_YEARS);
                } else if (calendarOfMonth == entryDateOfMonth) {
                    if (calendarOfDay < entryDateOfDay) {
                        startTime = this.getStartTimeForAddYear(entryDate, THREE_YEARS);
                    } else {
                        startTime = this.getStartTimeForAddYear(entryDate, FOUR_YEARS);
                    }
                } else {
                    startTime = this.getStartTimeForAddYear(entryDate, FOUR_YEARS);
                }
                response.setAnnualLeaveDays(FOURTEEN_DAYS);
                response.setStartTime(startTime);
                endTime = getEndTime(startTime);
                response.setEndTime(endTime);
            }
        } else if ((calendarOfYear - entryDateOfYear) >= FIVE_YEARS && (calendarOfYear - entryDateOfYear) < TEN_YEARS) {
            if ((calendarOfYear - entryDateOfYear) == FIVE_YEARS) {
                int tmpMonth = calendarOfMonth + FIVE_YEARS;
                // 入職超過4年，未滿5年，特休14日
                if ((tmpMonth - entryDateOfMonth) < FIVE_YEARS) {
                    response.setAnnualLeaveDays(FOURTEEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, FOUR_YEARS);
                } else if ((tmpMonth - entryDateOfMonth) == FIVE_YEARS) { // 入職月份滿5年，檢查日
                    // 入職超過4年，未滿5年，特休14日
                    if (calendarOfDay < entryDateOfDay) {
                        response.setAnnualLeaveDays(FOURTEEN_DAYS);
                        startTime = this.getStartTimeForAddYear(entryDate, FOUR_YEARS);
                    } else { // 入職剛滿或超過5年，特休15日
                        response.setAnnualLeaveDays(FIFTEEN_DAYS);
                        startTime = this.getStartTimeForAddYear(entryDate, FIVE_YEARS);
                    }
                } else { // 入職年份超過5年，特休15日
                    response.setAnnualLeaveDays(FIFTEEN_DAYS);
                    startTime = this.getStartTimeForAddYear(entryDate, FIVE_YEARS);
                }
                response.setStartTime(startTime);
                endTime = getEndTime(startTime);
                response.setEndTime(endTime);
            } else { // 入職年份超過5年(包含5年以上未滿6年、6年以上未滿7年...9年以上未滿10年)，未滿10年，特休15日
                if ((calendarOfYear - entryDateOfYear) == SIX_YEARS) {
                    if (calendarOfMonth < entryDateOfMonth) {
                        startTime = this.getStartTimeForAddYear(entryDate, FIVE_YEARS);
                    } else {
                        startTime = this.getStartTimeForAddYear(entryDate, SIX_YEARS);
                    }
                } else if ((calendarOfYear - entryDateOfYear) == SEVEN_YEARS) {
                    if (calendarOfMonth < entryDateOfMonth) {
                        startTime = this.getStartTimeForAddYear(entryDate, SIX_YEARS);
                    } else {
                        startTime = this.getStartTimeForAddYear(entryDate, SEVEN_YEARS
                        );
                    }
                } else if ((calendarOfYear - entryDateOfYear) == EIGHT_YEARS) {
                    if (calendarOfMonth < entryDateOfMonth) {
                        startTime = this.getStartTimeForAddYear(entryDate, SEVEN_YEARS);
                    } else {
                        startTime = this.getStartTimeForAddYear(entryDate, EIGHT_YEARS);
                    }
                } else if ((calendarOfYear - entryDateOfYear) == NINE_YEARS) {
                    if (calendarOfMonth < entryDateOfMonth) {
                        startTime = this.getStartTimeForAddYear(entryDate, EIGHT_YEARS);
                    } else {
                        startTime = this.getStartTimeForAddYear(entryDate, NINE_YEARS);
                    }
                }
                response.setAnnualLeaveDays(FIFTEEN_DAYS);
                response.setStartTime(startTime);
                endTime = getEndTime(startTime);
                response.setEndTime(endTime);
            }
        } else if ((calendarOfYear - entryDateOfYear) >= TEN_YEARS) {
            // 滿10年的基本特休天數
            int basicAnnualLeaveDays = SIXTEEN_DAYS;

            // 當前年份和入職年份差幾個月
            int currentYearMinusEntryYear = (calendarOfYear - entryDateOfYear) * 12;

            // 當前月份換算成入職那一年的月份
            int tmpCurrentMonth = calendarOfMonth + currentYearMinusEntryYear;

            // 實際的特休天數
            int annualLeaveDays;

            // 增加的特休天數
            int increasedDays = this.getIncreasedDays(calendarOfYear, entryDateOfYear);

            // 從入職日的年份到當前時間的年份增加幾年
            int increasedYears;

            //設定特休天數
            if ((tmpCurrentMonth - entryDateOfMonth) < currentYearMinusEntryYear) {
                annualLeaveDays = basicAnnualLeaveDays + increasedDays - ONE_DAY;
            } else if ((tmpCurrentMonth - entryDateOfMonth) == currentYearMinusEntryYear) {
                if (calendarOfDay < entryDateOfDay) {
                    annualLeaveDays = basicAnnualLeaveDays + increasedDays - ONE_DAY;
                } else {
                    annualLeaveDays = basicAnnualLeaveDays + increasedDays;
                }
            } else { // 滿10年之後，每滿1年多1天
                annualLeaveDays = basicAnnualLeaveDays + increasedDays;
            }

            //設定特休的開始時間
            if (calendarOfMonth < entryDateOfMonth) {
                increasedYears = calendarOfYear - entryDateOfYear - 1;
                startTime = this.getStartTimeForAddYear(entryDate, increasedYears);
            } else if (calendarOfMonth == entryDateOfMonth) {
                if (calendarOfDay < entryDateOfDay) {
                    increasedYears = calendarOfYear - entryDateOfYear - 1;
                    startTime = this.getStartTimeForAddYear(entryDate, increasedYears);
                } else {
                    increasedYears = calendarOfYear - entryDateOfYear;
                    startTime = this.getStartTimeForAddYear(entryDate, increasedYears);
                }
            } else {
                increasedYears = calendarOfYear - entryDateOfYear;
                startTime = this.getStartTimeForAddYear(entryDate, increasedYears);
            }

            // 特休天數超過30日，就取最大30日
            if (annualLeaveDays > MAXIMUM_ANNUAL_LEAVE_DAYS) {
                annualLeaveDays = MAXIMUM_ANNUAL_LEAVE_DAYS;
            }

            response.setAnnualLeaveDays(annualLeaveDays);
            response.setStartTime(startTime);
            endTime = getEndTime(startTime);
            response.setEndTime(endTime);
        }
        return response;
    }

    private Timestamp getStartTimeForAddMonth(Calendar entryDate, int month) {
        entryDate.add(Calendar.MONTH, month);
        return timeConvertService.calendarToTimestamp(entryDate);
    }

    private Timestamp getEndTimeForAddMonth(Calendar entryDate, int month) {
        entryDate.add(Calendar.MONTH, month);
        Timestamp endTime = timeConvertService.calendarToTimestamp(entryDate);
        return new Timestamp(endTime.getTime() - 1);
    }

    private Timestamp getStartTimeForAddYear(Calendar entryDate, int year) {
        entryDate.add(Calendar.YEAR, year);
        return timeConvertService.calendarToTimestamp(entryDate);
    }

    private Timestamp getEndTime(Timestamp startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.YEAR, ONE_YEAR);
        Timestamp endTime = timeConvertService.calendarToTimestamp(calendar);
        return new Timestamp(endTime.getTime() - 1);
    }

    /* 新增上一次的特休(因為同年度可能會有兩種特休天數) */
    private void addPreviousAnnualLeave(User user, Timestamp time, int annualLeaveDays) {
        Timestamp endTime = new Timestamp(time.getTime() - 1);
        List<Statistics> list = statisticsRepository.findUserPreviousAnnualLeave(user, Statistics.LeaveType.ANNUAL_LEAVE, endTime);
        if (list.isEmpty()) {
            Timestamp startTime;
            int oldAnnualLeaveDays;
            Calendar calendar = timeConvertService.timeStampToCalendar(time);

            // 當前特休3日
            if (annualLeaveDays == THREE_DAYS) {
                // 上次特休是2日
                oldAnnualLeaveDays = TWO_DAYS;

                // 開始時間往前推3個月
                calendar.add(Calendar.MONTH, -THREE_MONTH);
                startTime = timeConvertService.calendarToTimestamp(calendar);
                this.addStatistics(user, Statistics.LeaveType.ANNUAL_LEAVE, startTime, endTime, oldAnnualLeaveDays * EIGHT_HOURS);
            } else if (annualLeaveDays >= SEVEN_DAYS){ // 當前特休7日以上
                AddAnnualLeaveResponse response = this.getAnnualLeaveDays(user, timeConvertService.timeStampToCalendar(endTime));
                if (annualLeaveDays == SEVEN_DAYS) {
                    oldAnnualLeaveDays = response.getAnnualLeaveDays();

                    // 前次特休天數也是7日
                    if (oldAnnualLeaveDays == SEVEN_DAYS) {
                        // 開始時間往前推1年
                        calendar.add(Calendar.YEAR, -ONE_YEAR);
                        startTime = timeConvertService.calendarToTimestamp(calendar);
                    } else { // 前次特休天數是3日
                        // 開始時間往前推6個月
                        calendar.add(Calendar.MONTH, -SIX_MONTH);
                        startTime = timeConvertService.calendarToTimestamp(calendar);
                    }
                } else { // 特休10日以上
                    oldAnnualLeaveDays = response.getAnnualLeaveDays();
                    calendar.add(Calendar.YEAR, -ONE_YEAR);
                    startTime = timeConvertService.calendarToTimestamp(calendar);
                }
                this.addStatistics(user, Statistics.LeaveType.ANNUAL_LEAVE, startTime, endTime, oldAnnualLeaveDays * EIGHT_HOURS);
            }
        }
    }

    /**
     * 滿10年以上可能增加的特休天數(1~14日)
     */
    private int getIncreasedDays(int currentTimeOfYear, int entryDateOfYear) {
        // 特休增加1~9日
        int increasedDays = (currentTimeOfYear - entryDateOfYear) % 10;

        // 取得商數
        int quotient = (currentTimeOfYear - entryDateOfYear) / 10;

        // 特休增加10、11、12、13、14日
        if (quotient == 2) {
            increasedDays = increasedDays + 10;
        }
        return increasedDays;
    }

    /* 取得排班人員的例、休假天數 */
    @Override
    public void getShiftPersonnelCausalLeaveAndDayOff() {
        // 取得早、中、晚班排班人員
        List<User> allUsers = userRepository.findAll();
        List<User> dayShiftUsers = allUsers.stream().filter(user -> user.getClasses().getName().equals("早班")).collect(Collectors.toList());
        List<User> afternoonShiftUsers = allUsers.stream().filter(user -> user.getClasses().getName().equals("中班")).collect(Collectors.toList());
        List<User> nightShiftUsers = allUsers.stream().filter(user -> user.getClasses().getName().equals("晚班")).collect(Collectors.toList());
        List<User> shiftPersonnel = new ArrayList<>();
        shiftPersonnel.addAll(dayShiftUsers);
        shiftPersonnel.addAll(afternoonShiftUsers);
        shiftPersonnel.addAll(nightShiftUsers);

        // 取得Timestamp的當前時間
        Timestamp currentStartTime = new Timestamp(System.currentTimeMillis());
//        Timestamp currentStartTime = Timestamp.valueOf("2020-12-23 00:00:00.0"); // 測試用
        for (User user : shiftPersonnel) {
            /*
             * 設定排班人員的「例假」天數
             * 算法：該月星期日總天數即為例假天數
             * 1 查詢當月份的休假統計表是否有資料
             * 1-1 是：取得例假天數
             * 1-2 否：檢查到職日期年份是不是小於當前時間年份
             *      1-2-1 是(小於)：當月例假的起迄時間為一整個月
             *      1-2-2 否(等於)：檢查到職日期月份是不是小於當下時間月份
             *              1-2-2-1 是(小於)：當月例假的起迄時間為一整個月
             *              1-2-2-2 否(等於)：當月例假的開始時間為到職日期+排班的上班時間
             */
            try {
                // 取得當前時間的「年」和「月」
                int currentTimeOfYear = timeConvertService.getCalendarYear(currentStartTime);
                int currentTimeOfMonth = timeConvertService.getCalendarMonth(currentStartTime);

                // 取得入職時間的「年」和「月」
                Date entryDate = user.getEntryDate();
                int entryDateOfYear = timeConvertService.getCalendarYear(user.getEntryDate());
                int entryDateOfMonth = timeConvertService.getCalendarMonth(user.getEntryDate());

                // 「例假」的起、迄時間
                Timestamp startTime = Timestamp.valueOf(timeConvertService.getStartDate(currentTimeOfYear + "-" + currentTimeOfMonth).toString() + " " + "00:00:00.0");
                Timestamp endTime = timeConvertService.getEndTime(currentTimeOfYear + "-" + currentTimeOfMonth);

                // date用來取得某月的例假天數
                String date = timeConvertService.getStartDate(currentTimeOfYear + "-" + currentTimeOfMonth).toString();

                // 例假天數
                int causalLeaveDays = 0;

                // 1 查詢User某個時間區間是否有例假天數
                Optional<Statistics> causalLeaveStatisticsOptional = statisticsRepository.findByUserAndLeaveTypeAndStartTimeAfterAndEndTimeBefore(user, Statistics.LeaveType.CAUSAL_LEAVE, startTime, endTime);

                if (causalLeaveStatisticsOptional.isPresent()) {
                    // 1-1 取得例假天數
                    causalLeaveDays = causalLeaveStatisticsOptional.get().getHours() / EIGHT_HOURS;
                } else {
                    // 1-2 檢查到職日期年份是不是小於當前時間年份
                    if (entryDateOfYear < currentTimeOfYear) {
                        // 1-2-1 當月例假的起迄時間為一整個月
                        causalLeaveDays = this.getCausalLeaveDaysOfMonth(date);
                        this.addStatistics(user, Statistics.LeaveType.CAUSAL_LEAVE, startTime, endTime, causalLeaveDays * EIGHT_HOURS);
                    } else if (entryDateOfYear == currentTimeOfYear) {
                        // 1-2-2 檢查到職日期月份是不是小於當下時間月份
                        if (entryDateOfMonth < currentTimeOfMonth) {
                            // 1-2-2-1 當月例假的起迄時間為一整個月
                            causalLeaveDays = this.getCausalLeaveDaysOfMonth(date);
                            this.addStatistics(user, Statistics.LeaveType.CAUSAL_LEAVE, startTime, endTime, causalLeaveDays * EIGHT_HOURS);
                        } else if (entryDateOfMonth == currentTimeOfMonth) {
                            // 1-2-2-2 當月例假的開始時間為到職日期+排班的上班時間
                            Timestamp entryDateStartTime = Timestamp.valueOf(entryDate.toString() + " " + user.getClasses().getOnWorkTime().toString());
                            causalLeaveDays = this.getCausalLeaveDaysOfMonth(entryDate.toString());
                            this.addStatistics(user, Statistics.LeaveType.CAUSAL_LEAVE, entryDateStartTime, endTime, causalLeaveDays * EIGHT_HOURS);
                        }
                    }
                }

                /*
                 * 設定排班人員的「休假」天數
                 * 算法：該月假日天數-例假天數
                 * 2 查詢當月份的休假統計表是否有資料
                 * 2-1 是：檢查 holiday table 資料是否有更新
                 *       2-1-1 是：重設休假時數
                 *       2-1-2 否：不動作
                 * 2-2 否：檢查到職日期年份是不是小於當下時間年份
                 *       2-2-1 是(小於)：當月休假的起迄時間為一整個月
                 *       2-2-2 否(等於)：檢查到職日期月份是不是小於當下時間月份
                 *              2-2-2-1 是(小於)：當月休假的起迄時間為一整個月
                 *              2-2-2-2 否(等於)：當月休假的開始時間為到職日期+排班的上班時間
                 */
                // 休假天數
                int dayOffDays;

                // 2 查詢當月份的休假統計表是否有資料
                Optional<Statistics> dayOffStatisticsOptional = statisticsRepository.findByUserAndLeaveTypeAndStartTimeAfterAndEndTimeBefore(user, Statistics.LeaveType.DAY_OFF, startTime, endTime);


                if (dayOffStatisticsOptional.isPresent()) {
                    // 2-1 檢查 holiday table 資料是否有更新
                    Statistics dayOffStatistics = dayOffStatisticsOptional.get();
                    int tmpYear = timeConvertService.getCalendarYear(dayOffStatistics.getStartTime());
                    int tmpMonth = timeConvertService.getCalendarMonth(dayOffStatistics.getStartTime());
                    int tmpDay = timeConvertService.getCalendarDay(dayOffStatistics.getStartTime());
                    String strDate = tmpYear + "-" + tmpMonth + "-" + tmpDay;
                    dayOffDays = this.getDayOffDaysOfMonth(strDate, causalLeaveDays);
                    if (dayOffStatistics.getHours() != dayOffDays * EIGHT_HOURS) {
                        // 2-1-1 重設休假時數
                        this.updateStatisticsHours(dayOffStatistics.getId(), dayOffDays * EIGHT_HOURS);
                    }
                } else {
                    // 2-2 檢查到職日期年份是不是小於當下時間年份
                    if (entryDateOfYear < currentTimeOfYear) {
                        // 2-2-1 當月休假的起迄時間為一整個月
                        dayOffDays = this.getDayOffDaysOfMonth(date, causalLeaveDays);
                        this.addStatistics(user, Statistics.LeaveType.DAY_OFF, startTime, endTime, dayOffDays * EIGHT_HOURS);
                    } else if (entryDateOfYear == currentTimeOfYear) {
                        // 2-2-2 檢查到職日期月份是不是小於當下時間月份
                        if (entryDateOfMonth < currentTimeOfMonth) {
                            // 2-2-2-1 當月休假的起迄時間為一整個月
                            dayOffDays = this.getDayOffDaysOfMonth(date, causalLeaveDays);
                            this.addStatistics(user, Statistics.LeaveType.DAY_OFF, startTime, endTime, dayOffDays * EIGHT_HOURS);
                        } else if (entryDateOfMonth == currentTimeOfMonth) {
                            // 2-2-2-2 當月休假的開始時間為到職日期+排班的上班時間
                            Timestamp entryDatestartTime = Timestamp.valueOf(entryDate.toString() + " " + user.getClasses().getOnWorkTime().toString());
                            dayOffDays = this.getDayOffDaysOfMonth(entryDate.toString(), causalLeaveDays);
                            this.addStatistics(user, Statistics.LeaveType.DAY_OFF, entryDatestartTime, endTime, dayOffDays * EIGHT_HOURS);
                        }
                    }
                }
            } catch (TimestampErrorException | StatisticsNotExistsExcption e) {
                log.error(user.getAccount() + "  " + user.getName() + "：" + e.getMessage());
            }
        }
    }
}
