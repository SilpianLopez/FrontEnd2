package com.example.frontend2;

import com.google.gson.annotations.SerializedName;

public class CleaningList {
    @SerializedName("routine_id")
    private int routine_id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("repeat_unit")
    private String repeat_unit;

    @SerializedName("repeat_interval")
    private Integer repeat_interval;

    // 빈 생성자 (필요 시)
    public CleaningList() {}

    // Getter for routine ID
    public int getRoutine_id() {
        return routine_id;
    }

    // Setter for routine ID
    public void setRoutine_id(int routine_id) {
        this.routine_id = routine_id;
    }

    // 기존 필드에 대한 Getter/Setter
    public String getName() {
        return title;
    }

    public void setName(String title) {
        this.title = title;
    }

    public String getComment() {
        return description != null ? description : "";
    }

    public void setComment(String description) {
        this.description = description;
    }

    public String getCycle() {
        if (repeat_unit == null || repeat_interval == null) return "반복 없음";
        switch (repeat_unit) {
            case "DAY": return repeat_interval + "일";
            case "WEEK": return repeat_interval + "주";
            case "MONTH": return repeat_interval + "개월";
            default: return repeat_interval + " " + repeat_unit;
        }
    }

    public void setRepeat_unit(String repeat_unit) {
        this.repeat_unit = repeat_unit;
    }

    public void setRepeat_interval(Integer repeat_interval) {
        this.repeat_interval = repeat_interval;
    }
}
