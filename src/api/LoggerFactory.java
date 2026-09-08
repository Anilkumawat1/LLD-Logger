package api;

import core.LogManager;
import core.SimpleLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoggerFactory {
    private static final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();
    private LoggerFactory(){}
    public static Logger getLogger(Class<?> clazz) {
        return loggerMap.computeIfAbsent(clazz.getName(), name -> new SimpleLogger(name, LogManager.getInstance().getLoggerConfig()));
    }
}
