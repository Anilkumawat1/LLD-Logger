package core.formatter;

import core.LogEvent;

import java.io.PrintWriter;
import java.io.StringWriter;

public class JsonFormatter implements Formatter {

    @Override
    public String format(LogEvent event) {

        StringBuilder json = new StringBuilder();

        json.append("{")
                .append("\"timestamp\":\"")
                .append(escape(event.getTimestamp().toString()))
                .append("\",")

                .append("\"level\":\"")
                .append(escape(event.getLevel().toString()))
                .append("\",")

                .append("\"logger\":\"")
                .append(escape(event.getLoggerName()))
                .append("\",")

                .append("\"thread\":\"")
                .append(escape(event.getThreadName()))
                .append("\",")

                .append("\"message\":\"")
                .append(escape(event.getMessage()))
                .append("\"");

        if (event.getThrowable() != null) {

            Throwable throwable = event.getThrowable();

            json.append(",")
                    .append("\"exception\":{")

                    .append("\"type\":\"")
                    .append(escape(throwable.getClass().getName()))
                    .append("\",")

                    .append("\"message\":\"")
                    .append(escape(throwable.getMessage()))
                    .append("\",")

                    .append("\"stackTrace\":\"")
                    .append(escape(getStackTrace(throwable)))
                    .append("\"")

                    .append("}");
        }

        json.append("}");

        return json.toString();
    }

    private String getStackTrace(Throwable throwable) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);

        return stringWriter.toString();
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
