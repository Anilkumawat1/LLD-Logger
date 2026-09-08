package core;

import level.LogLevel;

import java.time.Instant;

public final class LogEvent {
    private final LogLevel level;
    private final String message;
    private final Instant timestamp;
    private final Throwable throwable;
    private final String loggerName;
    private final String threadName;

    public LogEvent(LogLevel level, String message, Instant timestamp, Throwable throwable, String loggerName, String threadName) {
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
        this.throwable = throwable;
        this.loggerName = loggerName;
        this.threadName = threadName;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getMessage() {
        return message;
    }

    public String getThreadName() {
        return threadName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
