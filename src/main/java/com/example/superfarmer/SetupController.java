package com.example.superfarmer;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class SetupController {

    @FXML
    private ComboBox<Integer> opponentCountBox;

    @FXML
    private Button startButton;

    @FXML
    public void initialize() {
        opponentCountBox.getItems().addAll(1, 2, 3);
    }

    @FXML
    private void startGame() {
        try {
            int opponentCount = opponentCountBox.getValue();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Parent root = loader.load();

            GameController controller = loader.getController();
            controller.setOpponentCount(opponentCount);
            controller.initGame();

            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
