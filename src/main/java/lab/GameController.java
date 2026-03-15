package lab;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.application.Platform;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class GameController {
    @FXML
    private Canvas canvas;

    private App app;
    private Track track;
    private DrawingThread drawingThread;
    private boolean running = false;

    private int currentScore = 0;
    private int currentLives = 3;

    void setApp(App app) {
        this.app = app;
    }

    @FXML
    void initialize() {
        bindInput();
    }

    // Nastavi reakcie na stlacenie klaves a riesi focus canvasu
    private void bindInput() {
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> {
            if (track != null) {
                track.handleKeyPressed(e.getCode());
            }
        });
        canvas.setOnKeyReleased(e -> {
            if (track != null) {
                track.handleKeyReleased(e.getCode());
            }
        });
    }

    // Vytvori novu instanciu hry (Track) a nastavi listenery pre game over a postup dalej
    public void prepareGame(int level, Difficulty difficulty) {
        track = new Track(canvas.getWidth(), canvas.getHeight(), level, difficulty, currentScore, currentLives);

        // Ak nastane game over, povieme to hlavnej aplikacii
        track.addGameOverListener(() -> {
            if (app != null) {
                app.onGameOver();
            }
        });

        // Ak hrac dokonci level, ulozime skore a povieme aplikacii nech ide dalej
        track.addLevelListener(() -> {
            this.currentScore = track.getScore();
            this.currentLives = track.getLives();
            Platform.runLater(() -> app.nextLevel());
        });

        drawingThread = null;
        drawOnce();
    }

    // Resetuje skore a zivoty a spusti hru uplne od zaciatku pre dany level
    public void startNewGame(int level, Difficulty difficulty) {
        Config cfg = Config.load();
        this.currentLives = cfg.getInitialLives();
        this.currentScore = 0;

        stopLoop();
        prepareGame(level, difficulty);
        startLoop();
        requestCanvasFocus();
    }

    // Vykresli aktualny stav hry jedenkrat (napr. pri inicializacii)
    public void drawOnce() {
        if (track != null) {
            track.draw(canvas.getGraphicsContext2D());
        }
    }

    // Spusti vlakno, ktore pravidelne aktualizuje a vykresluje hru
    public void startLoop() {
        if (running) {
            return;
        }
        drawingThread = new DrawingThread(canvas, track);
        drawingThread.start();
        running = true;
    }

    // Zastavi hernu slucku a vlakno
    public void stopLoop() {
        if (drawingThread != null) {
            drawingThread.stop();
            drawingThread = null;
        }
        running = false;
    }

    public void requestCanvasFocus() {
        canvas.requestFocus();
    }

    public void stop() {
        stopLoop();
    }
}
