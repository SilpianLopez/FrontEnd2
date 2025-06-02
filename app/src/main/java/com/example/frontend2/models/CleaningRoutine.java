package com.example.frontend2.models;

public class CleaningRoutine {
    private int routine_id;
    private int space_id;
    private int user_id;
    private String title;
    private String description;
    private String repeat_unit;         // 'DAY', 'WEEK', 'MONTH', etc.
    private Integer repeat_interval;    // null 가능
    private String last_cleaned_at;     // ISO 8601 datetime string
    private String next_due_date;       // 날짜만 (yyyy-MM-dd)

    // Getter
    public int getRoutine_id() {
        return routine_id;
    }

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

    public String getLast_cleaned_at() {
        return last_cleaned_at;
    }

    public String getNext_due_date() {
        return next_due_date;
    }

    // Setter
    public void setSpace_id(int space_id) {
        this.space_id = space_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRepeat_unit(String repeat_unit) {
        this.repeat_unit = repeat_unit;
    }

    public void setRepeat_interval(Integer repeat_interval) {
        this.repeat_interval = repeat_interval;
    }

    public void setLast_cleaned_at(String last_cleaned_at) {
        this.last_cleaned_at = last_cleaned_at;
    }

    public void setNext_due_date(String next_due_date) {
        this.next_due_date = next_due_date;
    }
}
