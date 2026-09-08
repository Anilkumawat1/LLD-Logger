package core.rotation;

public interface RotationStrategy {

    boolean shouldRotate(long currentFileSize, String formattedMessage);
}