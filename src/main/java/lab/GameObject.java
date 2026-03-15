package lab;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import lombok.ToString;
@ToString
public abstract class GameObject implements DrawableSimulable, Collisionable {
    @Getter
    protected double x, y;
    @Getter
    protected int cellSize;
    protected double offsetX, offsetY;

    public GameObject(double x, double y, int cellSize) {
        this.x = x;
        this.y = y;
        this.cellSize = cellSize;
    }


    @Override
    public abstract void draw(GraphicsContext gc);

    @Override
    public abstract void simulate(double deltaTime);

    @Override
    public abstract Rectangle2D getBoundingBox();

    public void setGridOffsets(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}
