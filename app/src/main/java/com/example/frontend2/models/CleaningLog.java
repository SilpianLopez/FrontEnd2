package com.example.frontend2.models;

public class CleaningLog {
    private int log_id;
    private int routine_id;
    private int user_id;
    private String cleaned_at; // 날짜+시간을 문자열로 받기

    // Getter
    public int getLog_id() {
        return log_id;
    }

    public int getRoutine_id() {
        return routine_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getCleaned_at() {
        return cleaned_at;
    }

    // Setter
    public void setRoutine_id(int routine_id) {
        this.routine_id = routine_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setCleaned_at(String cleaned_at) {
        this.cleaned_at = cleaned_at;
    }
}
