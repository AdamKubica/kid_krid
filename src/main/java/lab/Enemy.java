package lab;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ToString
public class Enemy extends GameObject {
    @Setter
    private double speed = 100;
    private int dirX = 0;
    private int dirY = 0;

    private final Grid grid;
    private final Random rnd = new Random();
    private final Image image;

    private double lastHitTime = -1;
    private static final double HIT_COOLDOWN = 0.5;

    private final double enemyWidth;
    private final double enemyHeight;

    public Enemy(double x, double y, int cellSize, Grid grid) {
        super(x, y, cellSize);
        this.grid = grid;

        image = new Image(Player.class.getResourceAsStream("enemy.png"));

        this.enemyWidth = cellSize * 0.4;
        this.enemyHeight = this.enemyWidth * (608.0 / 661.0);

        pickDirectionAtNode();
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x - enemyWidth / 2, y - enemyHeight / 2, enemyWidth, enemyHeight);
    }

    // Logika pohybu nepriatela a vyberu smeru v uzloch mriezky
    @Override
    public void simulate(double deltaTime) {
        if (dirX == 0 && dirY == 0) {
            pickDirectionAtNode();
            if (dirX == 0 && dirY == 0) {
                return;
            }
        }

        double moveDistance = speed * deltaTime;
        double distToNextNode = getDistToNextNode();

        if (moveDistance >= distToNextNode) {
            x += dirX * distToNextNode;
            y += dirY * distToNextNode;

            pickDirectionAtNode();

            double remaining = moveDistance - distToNextNode;
            if (dirX != 0 || dirY != 0) {
                x += dirX * remaining;
                y += dirY * remaining;
            }

        } else {
            x += dirX * moveDistance;
            y += dirY * moveDistance;
        }
    }

    // Vypocita vzdialenost k dalsiemu bodu zlomu
    private double getDistToNextNode() {
        double modX = (x - grid.getOffsetX()) % cellSize;
        double modY = (y - grid.getOffsetY()) % cellSize;

        if (Math.abs(modX) < 0.001) {
            modX = 0;
        }
        if (Math.abs(modY) < 0.001) {
            modY = 0;
        }

        if (modX < 0) {
            modX += cellSize;
        }
        if (modY < 0) {
            modY += cellSize;
        }

        if (dirX == 1) {
            return cellSize - modX;
        }
        if (dirX == -1) {
            if (modX == 0) {
                return cellSize;
            } else {
                return modX;
            }
        }
        if (dirY == 1) {
            return cellSize - modY;
        }
        if (dirY == -1) {
            if (modY == 0) {
                return cellSize;
            } else {
                return modY;
            }
        }

        return 0;
    }

    // Vyberie nahodny smer, ktorym sa nepriatel moze pohnut (aby nespadol do diery)
    private void pickDirectionAtNode() {
        List<int[]> candidates = new ArrayList<>();
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] d : directions) {
            int dx = d[0];
            int dy = d[1];
            boolean isReverse = (dx == -dirX && dy == -dirY);

            if (grid.canMove(x, y, dx, dy)) {
                if (!isReverse) {
                    candidates.add(d);
                }
            }
        }

        if (!candidates.isEmpty()) {
            int[] picked = candidates.get(rnd.nextInt(candidates.size()));
            dirX = picked[0];
            dirY = picked[1];
        } else {
            if (grid.canMove(x, y, -dirX, -dirY)) {
                dirX = -dirX;
                dirY = -dirY;
            } else {
                dirX = 0;
                dirY = 0;
            }
        }
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return new Rectangle2D(x - enemyWidth / 2, y - enemyHeight / 2, enemyWidth, enemyHeight);
    }

    public boolean canHit(double currentTime) {
        return (currentTime - lastHitTime) > HIT_COOLDOWN;
    }

    public void registerHit(double currentTime) {
        lastHitTime = currentTime;
    }

}
