package com.example.superfarmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.*;

public class GameController {

    private Player player;
    private Bank bank;

    @FXML
    private Label playerResources;
    @FXML
    private Label bankResources;
    @FXML
    private ComboBox<AnimalType> giveAnimal;
    @FXML
    private ComboBox<AnimalType> getAnimal;
    @FXML
    private TextField giveAmount;
    @FXML
    private Label diceResult;

    @FXML
    public void initialize() {
        player = new Player("Ty");
        bank = new Bank();

        giveAnimal.getItems().addAll(AnimalType.values());
        getAnimal.getItems().addAll(AnimalType.values());

        updateUI();
    }

    @FXML
    private void handleExchange() {
        try {
            int amount = Integer.parseInt(giveAmount.getText());
            AnimalType give = giveAnimal.getValue();
            AnimalType get = getAnimal.getValue();

            if (bank.exchange(player, give, get, amount)) {
                showAlert("Wymiana udana!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Nieudana wymiana", Alert.AlertType.ERROR);
            }
            updateUI();
        } catch (Exception e) {
            showAlert("Nieprawidłowe dane", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRollDice() {
        String roll = Dice.rollTwoDice();
        diceResult.setText("Wynik rzutu: " + roll);
    }

    private void updateUI() {
        playerResources.setText(player.getResources().toString());
        bankResources.setText(bank.getResources().toString());
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
