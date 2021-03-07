package com.example.util.logging;

public enum LogLevel {
    INFO(2), ERROR(3), DEBUG(1);

    private final int logLevel;

    LogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return logLevel;
    }
}
