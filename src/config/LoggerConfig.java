package config;

import core.appender.Appender;
import level.LogLevel;

import java.util.List;

public final class LoggerConfig {
    private final LogLevel logLevel;
    private final List<Appender> appenders;

    public LoggerConfig(LogLevel logLevel, List<Appender> appenders) {
        this.logLevel = logLevel;
        this.appenders = List.copyOf(appenders);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }
    public List<Appender> getAppenders() {
        return appenders;
    }
}
