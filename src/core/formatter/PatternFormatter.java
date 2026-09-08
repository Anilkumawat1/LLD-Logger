package core.formatter;

import core.LogEvent;
import core.formatter.Formatter;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PatternFormatter implements Formatter {

    @Override
    public String format(LogEvent event) {

        StringBuilder result = new StringBuilder();

        result.append(String.format(
                "%s [%s] %s - %s",
                event.getTimestamp(),
                event.getLevel(),
                event.getLoggerName(),
                event.getMessage()
        ));

        if (event.getThrowable() != null) {
            result.append(System.lineSeparator());
            result.append(getStackTrace(event.getThrowable()));
        }

        return result.toString();
    }

    private String getStackTrace(Throwable throwable) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);

        return stringWriter.toString();
    }
}