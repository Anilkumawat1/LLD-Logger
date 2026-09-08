import api.Logger;
import api.LoggerFactory;
import config.LoggerConfig;
import core.LogManager;
import core.appender.AsyncAppender;
import core.appender.ConsoleAppender;
import core.appender.FileAppender;
import core.filter.LevelFilter;
import core.formatter.JsonFormatter;
import core.formatter.PatternFormatter;
import level.LogLevel;

import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        LoggerConfig config = LoggerConfig.builder()
                .setLogLevel(LogLevel.INFO) // Root level for the logger
                .addAppender(new ConsoleAppender(new PatternFormatter(),List.of(new LevelFilter(LogLevel.ERROR))))
                .addAppender(new FileAppender("application.log", new JsonFormatter(),List.of(new LevelFilter(LogLevel.INFO))))
                .addAppender(new AsyncAppender(new FileAppender("async.log", new PatternFormatter(),List.of(new LevelFilter(LogLevel.INFO))), 10000))
                .build();

        LogManager logManager = LogManager.getInstance();
        logManager.setLoggerConfig(config);
        log.info("This is an info message from Main class. %s", "Additional Info");
        try {
            int result = 10 / 0; // This will throw an exception
        } catch (Exception e) {
            log.error("An exception occurred: %s",e, e.getMessage());
        }
    }
}