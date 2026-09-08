package core.appender;

import core.LogEvent;
import core.filter.Filter;
import core.formatter.Formatter;

import java.util.List;

public class ConsoleAppender extends AbstractAppender{
    public ConsoleAppender(Formatter formatter, List<Filter> filters) {
        super(formatter, filters);
    }

    @Override
    public void append(LogEvent logEvent) {
        if(!shouldAppend(logEvent)) {
            return;
        }
        String formattedMessage = formatter.format(logEvent);
        System.out.println(formattedMessage);
    }

    @Override
    public void close() {

    }
}
