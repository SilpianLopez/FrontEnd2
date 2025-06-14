package com.example.frontend2;

import java.util.List;

public class AlarmRoom {
    private String roomName;
    private List<AlarmTask> taskList;

    public AlarmRoom(String roomName, List<AlarmTask> taskList) {
        this.roomName = roomName;
        this.taskList = taskList;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<AlarmTask> getTaskList() {
        return taskList;
    }
}
