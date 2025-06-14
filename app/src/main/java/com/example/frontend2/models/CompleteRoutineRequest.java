package com.example.frontend2.models;

public class CompleteRoutineRequest {
    private int routine_id;
    private boolean is_complete;

    public CompleteRoutineRequest(int routine_id, boolean is_complete) {
        this.routine_id = routine_id;
        this.is_complete = is_complete;
    }

    public int getRoutine_id() {
        return routine_id;
    }

    public boolean isIs_complete() {
        return is_complete;
    }
}
