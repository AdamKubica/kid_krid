package lab;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Grid {
    @Getter
    private final int cols;
    @Getter
    private final int rows;
    @Getter
    private final int cellSize;
    @Getter
    private final double offsetX, offsetY;

    private final int canvasW, canvasH;
    private final boolean[][] hLines;
    private final boolean[][] vLines;
    private final boolean[][] cells;
    private final boolean[][] activeMap;
    private int filledCellsCount = 0;
    private int totalActiveCells = 0;
    private final boolean[][] trailMap;
    private WritableImage trailLayer;
    private PixelWriter pixelWriter;
    private static final int PADDING_TOP = 24;
    private static final int PADDING_BOTTOM = 24;
    private static final int PADDING_SIDE = 40;

    public Grid(int cols, int rows, double canvasWidth, double canvasHeight, boolean[][] shapeMap) {

        this.cols = cols;
        this.rows = rows;
        this.canvasW = (int) canvasWidth;
        this.canvasH = (int) canvasHeight;

        if (shapeMap == null) {
            this.activeMap = new boolean[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    this.activeMap[r][c] = true;
                }
            }
        } else {
            this.activeMap = shapeMap;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (this.activeMap[r][c]) {
                    totalActiveCells++;
                }
            }
        }

        int maxCellByWidth = (int) Math.floor((canvasWidth - (PADDING_SIDE * 2)) / cols);
        int maxCellByHeight = (int) Math.floor((canvasHeight - PADDING_TOP - PADDING_BOTTOM) / rows);

        this.cellSize = Math.min(maxCellByWidth, maxCellByHeight);

        this.offsetX = (canvasWidth - cols * cellSize) / 2.0;
        this.offsetY = (canvasHeight - rows * cellSize) / 2.0;

        hLines = new boolean[rows + 1][cols];
        vLines = new boolean[rows][cols + 1];
        cells = new boolean[rows][cols];

        trailMap = new boolean[canvasW][canvasH];
        trailLayer = new WritableImage(canvasW, canvasH);
        pixelWriter = trailLayer.getPixelWriter();
    }

    // Skontroluje, ci je mozny pohyb z danej pozicie urcenym smerom (ci tam je hrana)
    public boolean canMove(double currentX, double currentY, int dirX, int dirY) {
        int c = (int) Math.round((currentX - offsetX) / cellSize);
        int r = (int) Math.round((currentY - offsetY) / cellSize);

        if (dirX == 1) {
            return isActive(r - 1, c) || isActive(r, c);
        }
        if (dirX == -1) {
            return isActive(r - 1, c - 1) || isActive(r, c - 1);
        }
        if (dirY == 1) {
            return isActive(r, c - 1) || isActive(r, c);
        }
        if (dirY == -1) {
            return isActive(r - 1, c - 1) || isActive(r - 1, c);
        }
        return false;
    }

    // Skontroluje, ci su suradnice platnym bodom na mriezke
    public boolean isValidPosition(double x, double y) {
        int c = (int) Math.round((x - offsetX) / cellSize);
        int r = (int) Math.round((y - offsetY) / cellSize);

        if (c < 0 || c > cols || r < 0 || r > rows) {
            return false;
        }

        boolean topLeft = isActive(r - 1, c - 1);
        boolean topRight = isActive(r - 1, c);
        boolean bottomLeft = isActive(r, c - 1);
        boolean bottomRight = isActive(r, c);

        return topLeft || topRight || bottomLeft || bottomRight;
    }

    // Vrati ci je bunka na indexoch r, c sucastou mapy
    private boolean isActive(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return false;
        }
        return activeMap[r][c];
    }

    // Vrati percentualne zaplnenie plochy (0.0 az 1.0)
    public double getFilledPercent() {
        if (totalActiveCells == 0) {
            return 0;
        }
        return (double) filledCellsCount / totalActiveCells;
    }

    // Kresli bielu stopu do pomocnej vrstvy trailLayer na pozicii x, y
    public void paintTrail(double x, double y) {
        int ix = (int) x;
        int iy = (int) y;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int px = ix + i;
                int py = iy + j;
                if (px >= 0 && px < canvasW && py >= 0 && py < canvasH) {
                    pixelWriter.setArgb(px, py, 0xFFFFFFFF);
                    trailMap[px][py] = true;
                }
            }
        }
    }

    // Skontroluje, ci hrac uzavrel nejake bunky svojou stopou a vrati ziskane skore
    public int scanLinesAroundPlayer(double px, double py) {
        int c = (int) Math.floor((px - offsetX) / cellSize);
        int r = (int) Math.floor((py - offsetY) / cellSize);
        int score = 0;

        if (checkAndCompleteHorizontal(c, r)) {
            score += checkClosedCells(c, r);
            score += checkClosedCells(c, r - 1);
        }
        if (checkAndCompleteHorizontal(c, r + 1)) {
            score += checkClosedCells(c, r + 1);
            score += checkClosedCells(c, r);
        }
        if (checkAndCompleteVertical(c, r)) {
            score += checkClosedCells(c, r);
            score += checkClosedCells(c - 1, r);
        }
        if (checkAndCompleteVertical(c + 1, r)) {
            score += checkClosedCells(c + 1, r);
            score += checkClosedCells(c, r);
        }
        return score;
    }

    // Overi horizontalnu ciaru a ak je kompletne prekryta stopou, oznaci ju ako hotovu
    private boolean checkAndCompleteHorizontal(int c, int r) {
        if (c < 0 || c >= cols || r < 0 || r > rows) {
            return false;
        }
        if (hLines[r][c]) {
            return false;
        }

        double startX = offsetX + c * cellSize;
        double endX = startX + cellSize;
        double y = offsetY + r * cellSize;

        for (double x = startX + 4; x < endX - 4; x += 4) {
            if (!isTrailAt((int) x, (int) y)) {
                return false;
            }
        }
        hLines[r][c] = true;
        return true;
    }

    // Overi vertikalnu ciaru a ak je kompletne prekryta stopou, oznaci ju ako hotovu
    private boolean checkAndCompleteVertical(int c, int r) {
        if (c < 0 || c > cols || r < 0 || r >= rows) {
            return false;
        }
        if (vLines[r][c]) {
            return false;
        }

        double startY = offsetY + r * cellSize;
        double endY = startY + cellSize;
        double x = offsetX + c * cellSize;

        for (double y = startY + 4; y < endY - 4; y += 4) {
            if (!isTrailAt((int) x, (int) y)) {
                return false;
            }
        }
        vLines[r][c] = true;
        return true;
    }

    // Pomocna metoda na zistenie, ci je na danom pixeli stopa hraca
    private boolean isTrailAt(int x, int y) {
        if (x < 0 || x >= canvasW || y < 0 || y >= canvasH) {
            return true;
        }
        if (trailMap[x][y]) {
            return true;
        }
        if (trailMap[x + 1][y] || trailMap[x - 1][y] || trailMap[x][y + 1] || trailMap[x][y - 1]) {
            return true;
        }
        return false;
    }

    // Skontroluje, ci je bunka uzavreta zo vsetkych stran a ak ano, vyplni ju
    private int checkClosedCells(int c, int r) {
        if (!isActive(r, c)) {
            return 0;
        }
        if (cells[r][c]) {
            return 0;
        }

        if (hLines[r][c] && hLines[r + 1][c] && vLines[r][c] && vLines[r][c + 1]) {
            cells[r][c] = true;
            filledCellsCount++;
            return 1;
        }
        return 0;
    }

    // Vykresli mriezku, vyplnene bunky a stopu hraca na obrazovku
    public void draw(GraphicsContext gc) {
        gc.setLineDashes(4);
        gc.setLineWidth(1);
        gc.setStroke(Color.GRAY);

        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                double x = offsetX + c * cellSize;
                double y = offsetY + r * cellSize;

                if (c < cols) {
                    if (isActive(r - 1, c) || isActive(r, c)) {
                        gc.strokeLine(x, y, x + cellSize, y);
                    }
                }

                if (r < rows) {
                    if (isActive(r, c - 1) || isActive(r, c)) {
                        gc.strokeLine(x, y, x, y + cellSize);
                    }
                }
            }
        }

        gc.setLineDashes(null);
        gc.setFill(Color.YELLOW);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c]) {
                    gc.fillRect(offsetX + c * cellSize + 5, offsetY + r * cellSize + 5, cellSize - 10, cellSize - 10);
                }
            }
        }

        gc.drawImage(trailLayer, 0, 0);

        gc.setLineWidth(3);
        gc.setStroke(Color.WHITE);
        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (hLines[r][c]) {
                    double y = offsetY + r * cellSize;
                    gc.strokeLine(offsetX + c * cellSize, y, offsetX + (c + 1) * cellSize, y);
                }
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols; c++) {
                if (vLines[r][c]) {
                    double x = offsetX + c * cellSize;
                    gc.strokeLine(x, offsetY + r * cellSize, x, offsetY + (r + 1) * cellSize);
                }
            }
        }
    }

}
