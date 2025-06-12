package com.example.frontend2;

public class AlarmTask {
    private String taskName;
    private String alarmTime;
    private boolean alarmEnabled;

    public AlarmTask(String taskName, String alarmTime, boolean alarmEnabled) {
        this.taskName = taskName;
        this.alarmTime = alarmTime;
        this.alarmEnabled = alarmEnabled;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public boolean isAlarmEnabled() {
        return alarmEnabled;
    }

    public void setAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }
}
