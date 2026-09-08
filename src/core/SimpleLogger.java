package core;

import api.Logger;
import config.LoggerConfig;
import level.LogLevel;

import java.time.Instant;

public class SimpleLogger implements Logger {
    private final String loggerName;
    private final LoggerConfig loggerConfig;

    public SimpleLogger(String loggerName, LoggerConfig loggerConfig) {
        this.loggerName = loggerName;
        this.loggerConfig = loggerConfig;
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        log(LogLevel.ERROR, message,throwable, args);
    }

    private void log(LogLevel logLevel, String message, Throwable throwable, Object[] args) {
        if (logLevel.isEnabled(loggerConfig.getLogLevel())) {
            String formattedMessage = String.format(message, args);
            LogEvent logEvent = new LogEvent(
                    logLevel,
                    formattedMessage,
                    Instant.now(),
                    throwable,
                    loggerName,
                    Thread.currentThread().getName()
            );
            LogManager.getInstance().log(logEvent);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, null, args);
    }

    @Override
    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, null, args);
    }

    @Override
    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, null, args);
    }

    @Override
    public void trace(String message, Object... args) {
        log(LogLevel.TRACE, message, null, args);
    }

    @Override
    public void fatal(String message, Object... args) {
        log(LogLevel.FATAL, message, null, args);
    }
}
