package com.duongbanhat.pendingalarm.adapter;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Đối tượng dùng để lưu giá trị của một báo thức
 */
public class AlarmEntity implements Serializable  {
    @SerializedName("id")
    private long id;
    @SerializedName("year")
    private int year;
    @SerializedName("month")
    private int month;
    @SerializedName("day")
    private int day;
    @SerializedName("hour")
    private int hour;
    @SerializedName("minute")
    private int minute;
    @SerializedName("message")
    private String message;
    @SerializedName("time")
    private String time;
    @SerializedName("state")
    private String state;
    @SerializedName("longTime")
    private long longTime;

    public AlarmEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getLongTime() {
        return longTime;
    }

    public void setLongTime(long longTime) {
        this.longTime = longTime;
    }
}
