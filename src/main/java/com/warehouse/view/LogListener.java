package com.warehouse.view;

public interface LogListener {
    void onTaskLog(String message);

    void onMessageLog(String message);
}
