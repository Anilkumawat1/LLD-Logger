package core.formatter;

import core.LogEvent;

public interface Formatter {
    String format(LogEvent event);
}
