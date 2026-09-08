package core.appender;

import core.LogEvent;

public interface Appender {
    void append(LogEvent logEvent);
    void close();
}
