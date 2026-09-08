package core.rotation;

import java.nio.charset.StandardCharsets;

public class SizeRotationStrategy implements RotationStrategy {

    private final long maxFileSize;

    public SizeRotationStrategy(long maxFileSize) {
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException(
                    "Max file size must be greater than 0"
            );
        }

        this.maxFileSize = maxFileSize;
    }

    @Override
    public boolean shouldRotate(
            long currentFileSize,
            String formattedMessage) {

        long eventSize = (
                formattedMessage + System.lineSeparator()
        ).getBytes(StandardCharsets.UTF_8).length;

        return currentFileSize + eventSize > maxFileSize;
    }
}