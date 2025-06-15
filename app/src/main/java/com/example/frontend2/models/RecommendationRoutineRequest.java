package com.example.frontend2.models;

public class RecommendationRoutineRequest {
    private int user_id;
    private int space_id;
    private String title;
    private String repeat_unit;
    private Integer repeat_interval;

    public RecommendationRoutineRequest() {}

    // Getter & Setter

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getSpace_id() {
        return space_id;
    }

    public void setSpace_id(int space_id) {
        this.space_id = space_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRepeat_unit() {
        return repeat_unit;
    }

    public void setRepeat_unit(String repeat_unit) {
        this.repeat_unit = repeat_unit;
    }

    public Integer getRepeat_interval() {
        return repeat_interval;
    }

    public void setRepeat_interval(Integer repeat_interval) {
        this.repeat_interval = repeat_interval;
    }
}
