package core.filter;

import core.LogEvent;

public interface Filter {
    boolean accept(LogEvent logEvent);
}
