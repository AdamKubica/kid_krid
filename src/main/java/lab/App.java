package lab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class App extends Application {
    private static final int MAX_LEVEL = 5;

    public static void main(String[] args) {
        launch(args);
    }

    private GameController gameController;

    private Parent gameRoot;
    private Parent introRoot;
    private Parent levelsRoot;

    private int currentLevel = 1;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/lab/game.fxml"));
            gameRoot = gameLoader.load();
            gameController = gameLoader.getController();

            FXMLLoader introLoader = new FXMLLoader(getClass().getResource("/lab/intro.fxml"));
            introRoot = introLoader.load();
            IntroController introController = introLoader.getController();

            FXMLLoader levelsLoader = new FXMLLoader(getClass().getResource("/lab/levels.fxml"));
            levelsRoot = levelsLoader.load();
            LevelsController levelsController = levelsLoader.getController();

            gameController.setApp(this);
            introController.setApp(this);
            levelsController.setApp(this);

            StackPane rootStack = new StackPane(gameRoot, introRoot, levelsRoot);

            gameRoot.setVisible(false);
            levelsRoot.setVisible(false);
            introRoot.setVisible(true);

            Scene scene = new Scene(rootStack);

            URL cssUrl = getClass().getResource("application.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Kid Grid - The Game");
            primaryStage.show();

            primaryStage.setOnCloseRequest(this::exitProgram);

            log.debug("Všetky FXML scény boli úspešne načítané.");
        } catch (Exception e) {
            log.error("Kritická chyba pri štarte aplikácie, nepodarilo sa načítať UI!", e);
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        log.info("Obtiažnosť bola zmenená na: {}", difficulty);
    }

    public void showLevels() {
        introRoot.setVisible(false);
        levelsRoot.setVisible(true);
    }

    public void showIntro() {
        levelsRoot.setVisible(false);
        gameRoot.setVisible(false);
        introRoot.setVisible(true);
    }

    public void startLevel(int level) {
        log.info("Spúšťam level: {}", level);
        introRoot.setVisible(false);
        levelsRoot.setVisible(false);
        gameRoot.setVisible(true);

        currentLevel = level;
        gameController.startNewGame(level, currentDifficulty);
    }

    public void nextLevel() {
        gameController.stopLoop();
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            gameController.prepareGame(currentLevel, currentDifficulty);
            gameController.startLoop();
            gameController.requestCanvasFocus();
        } else {
            onGameOver();
        }
    }

    public void onGameOver() {
        gameController.stopLoop();
        gameRoot.setVisible(false);
        levelsRoot.setVisible(false);
        introRoot.setVisible(true);
    }

    @Override
    public void stop() throws Exception {
        gameController.stopLoop();
        super.stop();
    }

    private void exitProgram(WindowEvent evt) {
        System.exit(0);
    }
}
