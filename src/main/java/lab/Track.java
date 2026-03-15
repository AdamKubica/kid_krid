package lab;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2

public class Track {
    @Getter
    private int score;
    @Getter private int lives;

    private static final Config CONFIG = Config.load();
    private final Grid grid;
    private final Player player;
    private final List<Enemy> enemies = new ArrayList<>();

    private double elapsedTime = 0;
    private final Random RANDOM = new Random();
    private final Image heartImage;
    private final List<DrawableSimulable> entities = new ArrayList<>();
    private final List<LivesListener> livesListeners = new ArrayList<>();
    private final List<GameOverListener> gameOverListeners = new ArrayList<>();
    private final List<LevelListener> levelListeners = new ArrayList<>();
    private Comparator<DrawableSimulable> comparator;
    private int currentLevelNumber;

    public Track(double width, double height, int level, Difficulty difficulty, int currentScore, int currentLives) {
        log.info("Inicializácia mapy pre level {} s obtiažnosťou {}", level, difficulty);
        this.currentLevelNumber = level;
        this.score = currentScore;
        this.lives = currentLives;

        heartImage = new Image(Track.class.getResourceAsStream("heart.png"));

        int cols = CONFIG.getGridCols();
        int rows = CONFIG.getGridRows();

        boolean[][] levelShape = Levels.getLevelMap(level, cols, rows);

        grid = new Grid(cols, rows, width, height, levelShape);

        int cellSize = grid.getCellSize();

        double startX = grid.getOffsetX() + (cols / 2) * cellSize;
        double startY = grid.getOffsetY() + (rows / 2) * cellSize;

        if (!grid.isValidPosition(startX, startY)) {
            boolean found = false;
            for (int r = 0; r <= rows && !found; r++) {
                for (int c = 0; c <= cols && !found; c++) {
                    double tx = grid.getOffsetX() + c * cellSize;
                    double ty = grid.getOffsetY() + r * cellSize;
                    if (grid.isValidPosition(tx, ty)) {
                        startX = tx;
                        startY = ty;
                        found = true;
                    }
                }
            }
        }

        player = new Player(startX, startY, cellSize);
        player.setGrid(grid);
        player.setTrack(this);
        player.setSpeed(CONFIG.getPlayerSpeed());

        int enemyCount = 3;
        double enemySpeed = 100;

        switch (difficulty) {
            case EASY -> {
                enemyCount = 3;
                enemySpeed = 80;
            }
            case MEDIUM -> {
                enemyCount = 5;
                enemySpeed = 120;
            }
            case HARD -> {
                enemyCount = 8;
                enemySpeed = 180;
            }
        }

        for (int i = 0; i < enemyCount; i++) {
            Enemy e = spawnEnemy(cols, rows, cellSize, enemySpeed);
            enemies.add(e);
        }
        log.debug("Vytvorených {} nepriateľov s rýchlosťou {}", enemyCount, enemySpeed);

        entities.addAll(enemies);
        entities.add(player);

        comparator = (a, b) -> {
            if (a instanceof Player && !(b instanceof Player)) return 1;
            if (b instanceof Player && !(a instanceof Player)) return -1;
            return 0;
        };
        entities.sort(comparator);
    }

    // Najde nahodnu platnu poziciu na mriežke a vytvori tam noveho nepriatela
    private Enemy spawnEnemy(int cols, int rows, int cellSize, double speed) {
        while (true) {
            int r = RANDOM.nextInt(rows);
            int c = RANDOM.nextInt(cols);
            double x = grid.getOffsetX() + c * cellSize;
            double y = grid.getOffsetY() + r * cellSize;

            if (grid.isValidPosition(x, y)) {
                log.trace("Nepriateľ bol vygenerovaný na súradniciach [{}, {}]", x, y);
                Enemy e = new Enemy(x, y, cellSize, grid);
                e.setSpeed(speed);
                return e;
            }
        }
    }

    public void addScore(int points) {
        score += points;
    }

    public void draw(GraphicsContext gc) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        grid.draw(gc);

        for (DrawableSimulable e : entities) {
            e.draw(gc);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 20));
        gc.fillText("Score: " + score, canvasWidth - 100, 30);
        gc.fillText("Level: " + currentLevelNumber, 20, 30);

        int percent = (int) (grid.getFilledPercent() * 100);
        gc.fillText(percent + "% / 100%", 20, 60);

        double heartSize = 30;
        double xPos = canvasWidth - 60;
        double startY = 50;
        double gap = 5;

        for (int i = 0; i < lives; i++) {
            double yPos = startY + (i * (heartSize + gap));
            gc.drawImage(heartImage, xPos, yPos, heartSize, heartSize);
        }
    }

    // Skontroluje kolizie hraca s nepriatelmi a ak nastanu, znizi zivoty
    private void checkCollisions() {
        for (Enemy enemy : enemies) {
            if (player.getBoundingBox().intersects(enemy.getBoundingBox())) {
                if (enemy.canHit(elapsedTime) && lives > 0) {
                    decreaseLives();
                    enemy.registerHit(elapsedTime);
                }
            }
        }
    }

    // Hlavna simulacna metoda, aktualizuje entity a kontroluje stav levelu
    public void simulate(double delta) {
        elapsedTime += delta;
        for (DrawableSimulable e : entities) e.simulate(delta);

        if (grid.getFilledPercent() >= 0.999) {
            notifyLevelComplete();
        }
        checkCollisions();
    }

    public void addLivesListener(LivesListener l) {
        livesListeners.add(l);
    }

    public void removeLivesListener(LivesListener l) {
        livesListeners.remove(l);
    }

    public void addGameOverListener(GameOverListener l) {
        gameOverListeners.add(l);
    }

    public void removeGameOverListener(GameOverListener l) {
        gameOverListeners.remove(l);
    }

    public void addLevelListener(LevelListener l) {
        levelListeners.add(l);
    }

    public void removeLevelListener(LevelListener l) {
        levelListeners.remove(l);
    }

    private void notifyLivesChanged() {
        for (var l : livesListeners) l.onLivesChanged(lives);
        if (lives <= 0) for (var g : gameOverListeners) g.onGameOver();
    }

    private void notifyLevelComplete() {
        log.info("Level {} bol úspešne dokončený. Aktuálne skóre: {}", currentLevelNumber, score);
        for (var l : levelListeners) l.onLevelComplete();
    }

    public void decreaseLives() {
        lives = lives - 1;
        log.warn("Hráč stratil život! Zostávajúce životy: {}", lives);
        notifyLivesChanged();
    }

    public void handleKeyPressed(KeyCode code) {
        player.handleKeyPressed(code);
    }

    public void handleKeyReleased(KeyCode code) {
        player.handleKeyReleased(code);
    }

}
