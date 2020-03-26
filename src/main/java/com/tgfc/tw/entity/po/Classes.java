package com.tgfc.tw.entity.po;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Time;
import java.util.Set;

@Entity
@Table(name = "classes")
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "on_work_time")
    private Time onWorkTime;

    @NotNull
    @Column(name = "off_work_time")
    private Time offWorkTime;

    @Column(name = "flexible_time")
    private Time flexibleTime;

    @Column(name = "late_limit")
    private Time lateLimit;

    @Column(name = "early_limit")
    private Time earlyLimit;

    @NotNull
    @Column(name = "deadLine")
    private Time deadLine;

    //    @NotNull
    @Column(name = "color")
    private String color;

    @NotNull
    @Column(name = "use_calendar")
    private boolean useCalendar;

    @OneToMany(mappedBy = "classes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Dayoff> dayoffSet;

    @OneToMany(mappedBy = "classes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users;

    public Time getFlexibleTime() {
        return flexibleTime;
    }

    public void setFlexibleTime(Time flexibleTime) {
        this.flexibleTime = flexibleTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Time getOnWorkTime() {
        return onWorkTime;
    }

    public void setOnWorkTime(Time onWorkTime) {
        this.onWorkTime = onWorkTime;
    }

    public Time getOffWorkTime() {
        return offWorkTime;
    }

    public void setOffWorkTime(Time offWorkTime) {
        this.offWorkTime = offWorkTime;
    }

    public Time getLateLimit() {
        return lateLimit;
    }

    public void setLateLimit(Time lateLimit) {
        this.lateLimit = lateLimit;
    }

    public Time getEarlyLimit() {
        return earlyLimit;
    }

    public void setEarlyLimit(Time earlyLimit) {
        this.earlyLimit = earlyLimit;
    }

    public Time getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(Time deadLine) {
        this.deadLine = deadLine;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<Dayoff> getDayoffSet() {
        return dayoffSet;
    }

    public void setDayoffSet(Set<Dayoff> dayoffSet) {
        this.dayoffSet = dayoffSet;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public boolean isUseCalendar() {
        return useCalendar;
    }

    public void setUseCalendar(boolean useCalendar) {
        this.useCalendar = useCalendar;
    }
}
