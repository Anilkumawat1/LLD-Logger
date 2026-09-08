package config;

import core.appender.Appender;
import level.LogLevel;

import java.util.ArrayList;
import java.util.List;

public final class LoggerConfig {

    private final LogLevel logLevel;
    private final List<Appender> appenders;

    private LoggerConfig(
            LogLevel logLevel,
            List<Appender> appenders) {

        this.logLevel = logLevel;
        this.appenders = List.copyOf(appenders);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public List<Appender> getAppenders() {
        return appenders;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private LogLevel logLevel;
        private final List<Appender> appenders = new ArrayList<>();

        public Builder setLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder setAppenders(List<Appender> appenders) {
            this.appenders.clear();
            this.appenders.addAll(appenders);
            return this;
        }

        public Builder addAppender(Appender appender) {
            this.appenders.add(appender);
            return this;
        }

        public LoggerConfig build() {

            if (logLevel == null) {
                throw new IllegalStateException(
                        "Log level must be set"
                );
            }

            return new LoggerConfig(
                    logLevel,
                    appenders
            );
        }
    }
}