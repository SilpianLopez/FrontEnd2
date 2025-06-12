package com.example.frontend2.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RoutineRequest {
    private final int space_id;
    private final int user_id;
    private final String title;
    private final String description;
    private final String repeat_unit;
    private final Integer repeat_interval;
    private final String first_due_date; // "yyyy-MM-dd"

    private String weekdays;
    private String dates;

    // 기본 생성자 (요일/날짜 없이)
    public RoutineRequest(int space_id, int user_id, String title, String description,
                          String repeat_unit, Integer repeat_interval, String first_due_date) {
        this.space_id = space_id;
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.repeat_unit = repeat_unit;
        this.repeat_interval = repeat_interval;
        this.first_due_date = first_due_date;
    }

    // 요일 및 날짜 지정 포함 생성자
    public RoutineRequest(int space_id, int user_id, String title, String description,
                          String repeat_unit, Integer repeat_interval, String first_due_date,
                          String weekdays, String dates) {
        this.space_id = space_id;
        this.user_id = user_id;
        this.title = title;
        this.description = description;
        this.repeat_unit = repeat_unit;
        this.repeat_interval = repeat_interval;
        this.first_due_date = first_due_date;
        this.weekdays = weekdays;
        this.dates = dates;
    }

    // Getter
    public int getSpace_id() {
        return space_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRepeat_unit() {
        return repeat_unit;
    }

    public Integer getRepeat_interval() {
        return repeat_interval;
    }

    public String getFirst_due_date() {
        return first_due_date;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public String getDates() {
        return dates;
    }

}
