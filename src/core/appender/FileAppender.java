package core.appender;

import core.LogEvent;
import core.filter.Filter;
import core.formatter.Formatter;
import core.rotation.RotationManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileAppender extends AbstractAppender {

    private final Path filePath;
    private final RotationManager rotationManager;
    private final int maxBackupFiles;

    private BufferedWriter writer;

    public FileAppender(
            String filePath,
            Formatter formatter,
            List<Filter> filters,
            RotationManager rotationManager,
            int maxBackupFiles) {

        super(formatter, filters);

        if (maxBackupFiles < 0) {
            throw new IllegalArgumentException(
                    "Max backup files cannot be negative"
            );
        }

        this.filePath = Path.of(filePath);
        this.rotationManager = rotationManager;
        this.maxBackupFiles = maxBackupFiles;

        try {
            this.writer = new BufferedWriter(
                    new FileWriter(filePath, true)
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create FileAppender",
                    e
            );
        }
    }

    @Override
    public synchronized void append(LogEvent logEvent) {

        if (!shouldAppend(logEvent)) {
            return;
        }

        try {
            // Format the event only once
            String formattedMessage = formatter.format(logEvent);

            // Current file size in bytes
            long currentFileSize = Files.size(filePath);

            // Check rotation strategies
            if (rotationManager.shouldRotate(
                    currentFileSize,
                    formattedMessage)) {

                rotate();
            }

            // Write log
            writer.write(formattedMessage);
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to write log",
                    e
            );
        }
    }

    private void rotate() throws IOException {

        // Close current writer before moving the file
        writer.close();

        /*
         * Delete the oldest backup.
         *
         * Example:
         * maxBackupFiles = 3
         *
         * app.log.3 → deleted
         */
        if (maxBackupFiles > 0) {

            Path oldestBackup =
                    Path.of(filePath + "." + maxBackupFiles);

            Files.deleteIfExists(oldestBackup);

            /*
             * Shift backups:
             *
             * app.log.2 → app.log.3
             * app.log.1 → app.log.2
             */
            for (int i = maxBackupFiles - 1; i >= 1; i--) {

                Path source =
                        Path.of(filePath + "." + i);

                Path target =
                        Path.of(filePath + "." + (i + 1));

                if (Files.exists(source)) {
                    Files.move(
                            source,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }

            /*
             * Current log:
             *
             * app.log → app.log.1
             */
            Path firstBackup =
                    Path.of(filePath + ".1");

            if (Files.exists(filePath)) {
                Files.move(
                        filePath,
                        firstBackup,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } else {
            // No backups configured.
            // Just delete the current log file.
            Files.deleteIfExists(filePath);
        }

        // Create a fresh log file
        writer = new BufferedWriter(
                new FileWriter(String.valueOf(filePath), true)
        );
    }

    @Override
    public synchronized void close() {

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