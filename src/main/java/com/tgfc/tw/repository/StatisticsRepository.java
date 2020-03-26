package com.tgfc.tw.repository;

import com.tgfc.tw.entity.po.Statistics;
import com.tgfc.tw.entity.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Integer> {

    @Query(value = "select s from Statistics s where s.user = ?1 and s.leaveType = ?2 and s.endTime >= ?3")
    Optional<Statistics> findUserAnnualLeave(User user, Statistics.LeaveType leaveType, Timestamp time);

    @Query(value = "select s from Statistics s where s.user = ?1 and s.leaveType = ?2 and s.startTime < ?3")
    List<Statistics> findUserPreviousAnnualLeave(User user, Statistics.LeaveType leaveType, Timestamp time);

    @Query("select s from Statistics s where s.user = ?1 and s.leaveType = ?2 and s.startTime >= ?3 and s.endTime <= ?4")
    Optional<Statistics> findByUserAndLeaveTypeAndStartTimeAfterAndEndTimeBefore(User user, Statistics.LeaveType leaveType, Timestamp startTime, Timestamp endTime);

}
