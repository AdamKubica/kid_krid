package lab;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class IntroController {

    @FXML
    private Button btnEasy;
    @FXML
    private Button btnMedium;
    @FXML
    private Button btnHard;

    private App app;

    private Difficulty selectedDifficulty = Difficulty.MEDIUM;

    void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        updateButtons();
    }

    // Vyberie EASY obtiaznost a hned ju nastavi v hlavnej aplikacii
    @FXML
    private void selectEasy() {
        selectedDifficulty = Difficulty.EASY;
        updateButtons();
        if (app != null) {
            app.setDifficulty(selectedDifficulty);
        }
    }

    @FXML
    private void selectMedium() {
        selectedDifficulty = Difficulty.MEDIUM;
        updateButtons();
        if (app != null) {
            app.setDifficulty(selectedDifficulty);
        }
    }

    @FXML
    private void selectHard() {
        selectedDifficulty = Difficulty.HARD;
        updateButtons();
        if (app != null) {
            app.setDifficulty(selectedDifficulty);
        }
    }


    @FXML
    private void onLevels() {
        app.showLevels();
    }

    private void updateButtons() {
        btnEasy.getStyleClass().remove("selected-difficulty");
        btnMedium.getStyleClass().remove("selected-difficulty");
        btnHard.getStyleClass().remove("selected-difficulty");

        switch (selectedDifficulty) {
            case EASY -> btnEasy.getStyleClass().add("selected-difficulty");
            case MEDIUM -> btnMedium.getStyleClass().add("selected-difficulty");
            case HARD -> btnHard.getStyleClass().add("selected-difficulty");
        }
    }
}
