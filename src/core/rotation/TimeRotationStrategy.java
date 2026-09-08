package core.rotation;

public class TimeRotationStrategy implements RotationStrategy {

    private final long rotationIntervalMillis;
    private long lastRotationTime;

    public TimeRotationStrategy(long rotationIntervalMillis) {
        if (rotationIntervalMillis <= 0) {
            throw new IllegalArgumentException(
                    "Rotation interval must be greater than 0"
            );
        }

        this.rotationIntervalMillis = rotationIntervalMillis;
        this.lastRotationTime = System.currentTimeMillis();
    }

    @Override
    public synchronized boolean shouldRotate(
            long currentFileSize,
            String formattedMessage) {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRotationTime >= rotationIntervalMillis) {
            lastRotationTime = currentTime;
            return true;
        }

        return false;
    }
}
