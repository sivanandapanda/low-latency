package com.example.util.logging;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.util.logging.LogLevel.*;

public class Logger {

    private final boolean isAsync;
    private final LogLevel logLevel;
    private final ExecutorService asyncLogger;

    public Logger(LogLevel logLevel) {
        this(true, logLevel);
    }

    public Logger(boolean isAsync, LogLevel logLevel) {
        this.isAsync = isAsync;
        this.logLevel = logLevel;
        this.asyncLogger = Executors.newSingleThreadExecutor();
    }

    public void info(LocalDateTime dateTime, String message) {
        if(logLevel.getLogLevel() <= INFO.getLogLevel()) {
            String threadName = Thread.currentThread().getName();
            if(isAsync) {
                asyncLogger.submit(() -> System.out.println(dateTime + " INFO : " + threadName + " : " + message));
            } else {
                System.out.println(dateTime + " INFO : " + threadName + " : " + message);
            }
        }
    }

    public void debug(LocalDateTime dateTime, String message) {
        if(logLevel.getLogLevel() <= DEBUG.getLogLevel()) {
            String threadName = Thread.currentThread().getName();
            if(isAsync) {
                asyncLogger.submit(() -> System.out.println(dateTime + " DEBUG : " + threadName + " : " + message));
            } else {
                System.out.println(dateTime + " DEBUG : " + threadName + " : " + message);
            }
        }
    }

    public void error(LocalDateTime dateTime, String message, Throwable throwable) {
        if(logLevel.getLogLevel() <= ERROR.getLogLevel()) {
            String threadName = Thread.currentThread().getName();
            if(isAsync) {
                asyncLogger.submit(() -> {
                    System.err.println(dateTime + " ERROR : " + threadName + " : " + message);
                    throwable.printStackTrace();
                });
            } else {
                System.err.println(dateTime + " ERROR : " + threadName + " : " + message);
                throwable.printStackTrace();
            }
        }
    }

    public void stop() {
        asyncLogger.shutdownNow();
    }
}
