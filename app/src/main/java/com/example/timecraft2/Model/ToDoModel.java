package com.example.timecraft2.Model;

public class ToDoModel extends TaskId {

    private String task , due,ImageUrl;
    private int status;


    public String getTask() {
        return task;
    }

    public String getDue() {
        return due;
    }

    public int getStatus() {
        return status;
    }

    public String getTaskId() {
        return TaskId;
    }
}