package lab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Config {

    private static final String FILE_NAME = "kidgrid.properties";

    private static final int DEFAULT_GRID_COLS = 7;
    private static final int DEFAULT_GRID_ROWS = 5;

    private static final int DEFAULT_ENEMY_COUNT = 5;
    private static final int DEFAULT_INITIAL_LIVES = 3;
    private static final double DEFAULT_PLAYER_SPEED = 120.0;
    private static final double DEFAULT_ENEMY_SPEED = 100.0;

    private int gridCols;
    private int gridRows;
    private int enemyCount;
    private int initialLives;
    private double playerSpeed;
    private double enemySpeed;


    public static Config load() {
        log.debug("Pokúšam sa načítať konfiguráciu zo súboru: {}", FILE_NAME);
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            log.info("Súbor s konfiguráciou neexistuje, vytváram predvolený.");
            Config cfg = new Config(
                DEFAULT_GRID_COLS,
                DEFAULT_GRID_ROWS,
                DEFAULT_ENEMY_COUNT,
                DEFAULT_INITIAL_LIVES,
                DEFAULT_PLAYER_SPEED,
                DEFAULT_ENEMY_SPEED
            );
            cfg.save();
            return cfg;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            log.debug("Konfigurácia bola úspešne načítaná.");
        } catch (IOException e) {
            log.warn("Chyba pri čítaní {}, použijú sa predvolené hodnoty.", FILE_NAME, e);
        }

        int gridCols = parseInt(props.getProperty("grid.cols"), DEFAULT_GRID_COLS);
        int gridRows = parseInt(props.getProperty("grid.rows"), DEFAULT_GRID_ROWS);
        int enemyCount = parseInt(props.getProperty("enemy.count"), DEFAULT_ENEMY_COUNT);
        int initialLives = parseInt(props.getProperty("initial.lives"), DEFAULT_INITIAL_LIVES);
        double playerSpeed = parseDouble(props.getProperty("player.speed"), DEFAULT_PLAYER_SPEED);
        double enemySpeed = parseDouble(props.getProperty("enemy.speed"), DEFAULT_ENEMY_SPEED);

        return new Config(gridCols, gridRows, enemyCount, initialLives, playerSpeed, enemySpeed);
    }

    public void save() {
        log.debug("Ukladám konfiguráciu do súboru: {}", FILE_NAME);
        Properties props = new Properties();
        props.setProperty("grid.cols", Integer.toString(gridCols));
        props.setProperty("grid.rows", Integer.toString(gridRows));
        props.setProperty("enemy.count", Integer.toString(enemyCount));
        props.setProperty("initial.lives", Integer.toString(initialLives));
        props.setProperty("player.speed", Double.toString(playerSpeed));
        props.setProperty("enemy.speed", Double.toString(enemySpeed));

        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "Kid Grid configuration");
        } catch (IOException e) {
            log.error("Nepodarilo sa uložiť konfiguráciu do súboru!", e);
        }
    }

    private static int parseInt(String v, int def) {
        if (v == null) return def;

        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double parseDouble(String v, double def) {
        if (v == null) return def;

        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public int getGridCols() {
        return gridCols;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public int getInitialLives() {
        return initialLives;
    }

    public double getPlayerSpeed() {
        return playerSpeed;
    }

    public double getEnemySpeed() {
        return enemySpeed;
    }

    public void setGridCols(int gridCols) {
        this.gridCols = gridCols;
    }

    public void setGridRows(int gridRows) {
        this.gridRows = gridRows;
    }

    public void setEnemyCount(int enemyCount) {
        this.enemyCount = enemyCount;
    }

    public void setInitialLives(int initialLives) {
        this.initialLives = initialLives;
    }

    public void setPlayerSpeed(double playerSpeed) {
        this.playerSpeed = playerSpeed;
    }

    public void setEnemySpeed(double enemySpeed) {
        this.enemySpeed = enemySpeed;
    }
}
