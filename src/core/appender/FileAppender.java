package core.appender;

import core.LogEvent;
import core.filter.Filter;
import core.formatter.Formatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class FileAppender extends AbstractAppender{
    private final BufferedWriter writer;
    public FileAppender(String filePath, Formatter  formatter, List<Filter> filters) {
        super(formatter, filters);
        try{
            this.writer = new BufferedWriter(new FileWriter(filePath, true));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FileAppender", e);
        }
    }
    @Override
    public synchronized void append(LogEvent logEvent) {
        if(!shouldAppend(logEvent)) {
            return;
        }
        try {
            writer.write(formatter.format(logEvent));
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to write log",
                    e
            );
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to close log file",
                    e
            );
        }
    }
}
