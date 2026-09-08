package core;

import config.LoggerConfig;
import core.appender.Appender;
import level.LogLevel;

import java.util.List;

public class LogManager {

    private static final LogManager INSTANCE = new LogManager();
    private volatile LoggerConfig loggerConfig;
    private LogManager() {
        this.loggerConfig = new LoggerConfig(LogLevel.INFO,List.of());
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public void log(LogEvent logEvent) {
        for (Appender appender : loggerConfig.getAppenders()) {
            appender.append(logEvent);
        }
    }

    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    public void setLoggerConfig(LoggerConfig loggerConfig) {
        this.loggerConfig = loggerConfig;
    }
}
