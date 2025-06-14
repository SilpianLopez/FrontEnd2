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

    // 기본 생성자
    public CleaningList() {}

    // routine_id Getter/Setter
    public int getRoutine_id() {
        return routine_id;
    }
    public void setRoutine_id(int routine_id) {
        this.routine_id = routine_id;
    }

    // title 필드 (UI 표시용 getName/setName)
    public String getName() {
        return title;
    }
    public void setName(String title) {
        this.title = title;
    }

    // description 필드 (UI 표시용 getComment/setComment)
    public String getComment() {
        return description != null ? description : "";
    }
    public void setComment(String description) {
        this.description = description;
    }

    // 반복 주기 표시용
    public String getCycle() {
        if (repeat_unit == null || repeat_interval == null) return "반복 없음";
        switch (repeat_unit) {
            case "DAY":   return repeat_interval + "일";
            case "WEEK":  return repeat_interval + "주";
            case "MONTH": return repeat_interval + "개월";
            default:       return repeat_interval + " " + repeat_unit;
        }
    }
    public void setRepeat_unit(String repeat_unit) {
        this.repeat_unit = repeat_unit;
    }
    public void setRepeat_interval(Integer repeat_interval) {
        this.repeat_interval = repeat_interval;
    }
}
