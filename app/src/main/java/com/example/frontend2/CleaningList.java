package com.example.frontend2;

public class CleaningList {
    private String title;
    private String description;
    private String repeat_unit;
    private Integer repeat_interval;

    public String getName() {
        return title;
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

    public String getComment() {
        return description != null ? description : "";
    }
}
