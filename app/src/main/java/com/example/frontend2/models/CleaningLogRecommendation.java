package com.example.frontend2.models;

import com.google.gson.annotations.SerializedName;

public class CleaningLogRecommendation {

    @SerializedName("space_id")
    private int space_id;

    @SerializedName("space_name")
    private String space_name;

    @SerializedName("clean_count")
    private int clean_count;

    public int getSpace_id() {
        return space_id;
    }

    public String getSpace_name() {
        return space_name;
    }

    public int getClean_count() {
        return clean_count;
    }

}
