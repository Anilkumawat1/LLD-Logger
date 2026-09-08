package core.appender;

import core.LogEvent;
import core.filter.Filter;
import core.formatter.Formatter;

import java.util.List;

public abstract class AbstractAppender implements Appender {

    protected final Formatter formatter;
    protected final List<Filter> filters;

    protected AbstractAppender(
            Formatter formatter,
            List<Filter> filters) {

        this.formatter = formatter;
        this.filters = List.copyOf(filters);
    }

    protected boolean shouldAppend(LogEvent event) {

        for (Filter filter : filters) {
            if (!filter.accept(event)) {
                return false;
            }
        }

        return true;
    }
}
