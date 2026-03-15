package lab;

import javafx.fxml.FXML;

public class LevelsController {
    private App app;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    private void onLevel1() {
        app.startLevel(1);
    }

    @FXML
    private void onLevel2() {
        app.startLevel(2);
    }

    @FXML
    private void onLevel3() {
        app.startLevel(3);
    }

    @FXML
    private void onLevel4() {
        app.startLevel(4);
    }

    @FXML
    private void onLevel5() {
        app.startLevel(5);
    }

    @FXML
    private void onBack() {
        app.showIntro();
    }
}
