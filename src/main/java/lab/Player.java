package lab;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString
public class Player extends GameObject implements Collisionable {
    @Setter
    private double speed = 120;
    private final Image image;
    private int dirX = 0;
    private int dirY = 0;
    private int reqDirX = 0;
    private int reqDirY = 0;

    private boolean started = false;
    @Setter
    private Grid grid;
    @Setter
    private Track track;

    private double playerWidth;
    private double playerHeight;

    public Player(double x, double y, int cellSize) {
        super(x, y, cellSize);
        image = new Image(Player.class.getResourceAsStream("player.png"));

        this.playerWidth = cellSize * 0.5;
        this.playerHeight = this.playerWidth * (607.0 / 800.0);
    }

    public void handleKeyPressed(KeyCode code) {
        started = true;
        switch (code) {
            case UP -> setReqDirection(0, -1);
            case DOWN -> setReqDirection(0, 1);
            case LEFT -> setReqDirection(-1, 0);
            case RIGHT -> setReqDirection(1, 0);
            default -> setReqDirection(0, 0);
        }
    }

    public void handleKeyReleased(KeyCode code) {
        int releasedX = 0;
        int releasedY = 0;
        switch (code) {
            case UP -> releasedY = -1;
            case DOWN -> releasedY = 1;
            case LEFT -> releasedX = -1;
            case RIGHT -> releasedX = 1;
            default -> releasedY = 0;
        }
        if (reqDirX == releasedX && reqDirY == releasedY) {
            reqDirX = 0;
            reqDirY = 0;
        }
    }

    private void setReqDirection(int x, int y) {
        this.reqDirX = x;
        this.reqDirY = y;
    }

    // Hlavna logika pohybu hraca, kreslenia stopy a vyplnania uzemia
    @Override
    public void simulate(double deltaTime) {
        if (!started) {
            return;
        }

        if (reqDirX == -dirX && reqDirY == -dirY && (reqDirX != 0 || reqDirY != 0)) {
            dirX = reqDirX;
            dirY = reqDirY;
        }

        if (dirX == 0 && dirY == 0) {
            if (reqDirX != 0 || reqDirY != 0) {
                if (canMove(x, y, reqDirX, reqDirY)) {
                    dirX = reqDirX;
                    dirY = reqDirY;
                }
            }
        }

        if (dirX == 0 && dirY == 0) {
            return;
        }

        double moveDistance = speed * deltaTime;
        double distToNextNode = getDistToNextNode();

        if (grid != null) {
            double paintDist = Math.min(moveDistance, distToNextNode);
            int steps = (int) Math.ceil(paintDist);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double dx = x + (dirX * paintDist * t);
                double dy = y + (dirY * paintDist * t);
                grid.paintTrail(dx, dy);
            }

            int p = grid.scanLinesAroundPlayer(x, y);
            if (p > 0 && track != null) {
                track.addScore(p);
            }
        }

        if (moveDistance >= distToNextNode) {
            x += dirX * distToNextNode;
            y += dirY * distToNextNode;

            if (grid != null) {
                grid.paintTrail(x, y);
                int p = grid.scanLinesAroundPlayer(x, y);
                if (p > 0 && track != null) {
                    track.addScore(p);
                }
            }

            boolean changedDir = false;
            if (reqDirX != 0 || reqDirY != 0) {
                if (canMove(x, y, reqDirX, reqDirY)) {
                    dirX = reqDirX;
                    dirY = reqDirY;
                    changedDir = true;
                }
            }

            if (!changedDir) {
                if (!canMove(x, y, dirX, dirY)) {
                    dirX = 0;
                    dirY = 0;
                }
            }

            double remainingDist = moveDistance - distToNextNode;
            if (dirX != 0 || dirY != 0) {
                x += dirX * remainingDist;
                y += dirY * remainingDist;
                if (grid != null) {
                    grid.paintTrail(x, y);
                }
            }

        } else {
            x += dirX * moveDistance;
            y += dirY * moveDistance;
        }
    }

    // Vypocita vzdialenost k najblizsiemu uzlu mriezky v smere pohybu
    private double getDistToNextNode() {
        double modX = (x - grid.getOffsetX()) % grid.getCellSize();
        double modY = (y - grid.getOffsetY()) % grid.getCellSize();

        if (Math.abs(modX) < 0.0001) {
            modX = 0;
        }
        if (Math.abs(modY) < 0.0001) {
            modY = 0;
        }

        if (modX < 0) {
            modX += grid.getCellSize();
        }
        if (modY < 0) {
            modY += grid.getCellSize();
        }

        if (dirX == 1) {
            return grid.getCellSize() - modX;
        }

        if (dirX == -1) {
            if (modX == 0) {
                return grid.getCellSize();
            } else {
                return modX;
            }
        }

        if (dirY == 1) {
            return grid.getCellSize() - modY;
        }

        if (dirY == -1) {
            if (modY == 0) {
                return grid.getCellSize();
            } else {
                return modY;
            }
        }

        return 0;
    }

    // Skontroluje cez mriezku, ci sa hrac moze pohnut danym smerom
    private boolean canMove(double startX, double startY, int dX, int dY) {
        if (grid == null) {
            return false;
        }
        return grid.canMove(startX, startY, dX, dY);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.drawImage(image, x - playerWidth / 2, y - playerHeight / 2, playerWidth, playerHeight);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return new Rectangle2D(x - playerWidth / 2, y - playerHeight / 2, playerWidth, playerHeight);
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
