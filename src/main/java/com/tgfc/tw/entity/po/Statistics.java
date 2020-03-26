package com.tgfc.tw.entity.po;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "statistics")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    private User user;

    @Column(name = "hours")
    private int hours;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private LeaveType leaveType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public enum LeaveType {
        ANNUAL_LEAVE("特休"),
        SICK_LEAVE("病假"),
        PERSONAL_LEAVE("事假"),
        OFFICIAL_LEAVE("公假"),
        MARRIAGE_LEAVE("婚假"),
        FUNERAL_LEAVE("喪假"),
        MENSTRUAL_LEAVE("生理假"),
        MATERNITY_LEAVE("產假"),
        PATERNITY_LEAVE("陪產假"),
        COMPENSATORY_LEAVE("補休"),
        CAUSAL_LEAVE("例假"),
        DAY_OFF("休假");

        private String str;

        LeaveType(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }
}
