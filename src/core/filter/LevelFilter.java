package core.filter;

import core.LogEvent;
import level.LogLevel;

public class LevelFilter implements Filter {
    private final LogLevel level;

    public LevelFilter(LogLevel level) {
        this.level = level;
    }

    @Override
    public boolean accept(LogEvent logEvent) {
        return logEvent.getLevel().isEnabled(level);
    }
}
