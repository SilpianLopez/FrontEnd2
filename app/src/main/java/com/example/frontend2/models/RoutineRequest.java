package com.example.frontend2.models;

public class RoutineRequest {
    private final int space_id;
    private final int user_id;
    private final String title;
    private final String description;
    private final String repeat_unit;
    private final Integer repeat_interval;
    private final String first_due_date; // "yyyy-MM-dd"

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

    // 필요하면 getter/setter 추가
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

}

