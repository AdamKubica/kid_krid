package lab;

import javafx.geometry.Rectangle2D;

public interface Collisionable {

    Rectangle2D getBoundingBox();

    default boolean intersect(Rectangle2D another) {
        return getBoundingBox().intersects(another);
    }
}
