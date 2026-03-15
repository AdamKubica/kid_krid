package lab;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class DrawingThread extends AnimationTimer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Track track;
    private long lastFrame = 0;

    public DrawingThread(Canvas canvas, Track track) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.track = track;
    }

    @Override
    public void handle(long now) {
        double delta = lastFrame == 0 ? 0 : (now - lastFrame) / 1_000_000_000.0;
        lastFrame = now;

        track.simulate(delta);
        track.draw(gc);
    }
}
