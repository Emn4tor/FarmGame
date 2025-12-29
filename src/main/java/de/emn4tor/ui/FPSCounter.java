package de.emn4tor.ui;

public class FPSCounter {

    private double accumulatedTime = 0.0;
    private int frames = 0;
    private double fps = 0.0;

    public void update(double deltaTime) {
        accumulatedTime += deltaTime;
        frames++;

        if (accumulatedTime >= 1.0) {
            fps = frames / accumulatedTime;
            frames = 0;
            accumulatedTime = 0.0;
        }
    }

    public int getFPS() {
        return (int) fps;
    }
}
