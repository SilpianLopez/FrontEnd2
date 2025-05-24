package com.example.frontend.models;

public class Notification {
    private int notification_id;
    private int user_id;
    private int routine_id;
    private String alarm_date; // 날짜는 문자열(String)로 받아도 충분히 처리 가능

    // Getter
    public int getNotification_id() {
        return notification_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public int getRoutine_id() {
        return routine_id;
    }

    public String getAlarm_date() {
        return alarm_date;
    }

    // Setter
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setRoutine_id(int routine_id) {
        this.routine_id = routine_id;
    }

    public void setAlarm_date(String alarm_date) {
        this.alarm_date = alarm_date;
    }
}
