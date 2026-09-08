package core.rotation;

import java.util.List;

public class RotationManager {

    private final List<RotationStrategy> strategies;

    public RotationManager(List<RotationStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public boolean shouldRotate(
            long currentFileSize,
            String formattedMessage) {

        boolean shouldRotate = false;

        for (RotationStrategy strategy : strategies) {
            if (strategy.shouldRotate(
                    currentFileSize,
                    formattedMessage)) {

                shouldRotate = true;
            }
        }

        return shouldRotate;
    }
}