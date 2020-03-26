package com.tgfc.tw.entity.po;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "dayoff")
public class Dayoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "classes_id")
    private Classes classes;

    @ManyToOne
    @JoinColumn(name = "account", nullable = false)
    private User user;

    @Column(name = "date")
    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type")
    private Statistics.LeaveType leaveType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Classes getClasses() {
        return classes;
    }

    public void setClasses(Classes classes) {
        this.classes = classes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Statistics.LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(Statistics.LeaveType leaveType) {
        this.leaveType = leaveType;
    }
}
