package com.example.superfarmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;

import java.util.*;

public class GameController {

    private Player player;
    private Bank bank;
    private List<Player> opponents = new ArrayList<>();
    private List<Player> allPlayers = new ArrayList<>();
    private int currentPlayerIndex = 0;
    private int opponentCount = 2;
    private boolean canExchange = true;

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
    private VBox opponentsBox;

    @FXML
    private Button rollBtn;

    @FXML
    public void initialize() {
        giveAnimal.getItems().addAll(AnimalType.values());
        getAnimal.getItems().addAll(AnimalType.values());
    }

    public void setOpponentCount(int count) {
        this.opponentCount = count;
    }

    public void initGame() {
        player = new Player("Ty");
        bank = new Bank();

        for (int i = 1; i <= opponentCount; i++) {
            opponents.add(new Player("Bot " + i));
        }

        allPlayers.clear();
        allPlayers.add(player);
        allPlayers.addAll(opponents);

        updateUI();
    }

    @FXML
    private void handleExchange() {
        if (!canExchange) {
            showAlert("Możesz wymienić tylko raz na turę – przed rzutem!", Alert.AlertType.WARNING);
            return;
        }

        int amount;
        AnimalType give;
        AnimalType get;

        try {
            amount = Integer.parseInt(giveAmount.getText());
            give = giveAnimal.getValue();
            get = getAnimal.getValue();

            if (bank.exchange(player, give, get, amount)) {
                showAlert("Wymiana udana!", Alert.AlertType.INFORMATION);
                canExchange = false;
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
        for (int i = 0; i < allPlayers.size(); i++) {
            Player currentPlayer = allPlayers.get(i);
            processTurn(currentPlayer);
        }

        canExchange = true;
    }

    private void processTurn(Player player) {
        String[] result = Dice.rollTwoDice();

        if (player == this.player) {
            diceResult.setText("Twój rzut: " + result[0] + " + " + result[1]);
        } else {
            showAlert(player.getName() + " rzuca: " + result[0] + " + " + result[1], Alert.AlertType.INFORMATION);
        }

        handleThreats(player, result);
        handleBreeding(player, result);

        if (player != this.player) {
            autoExchange(player);
        }

        updateUI();

        if (checkVictory(player)) {
            String msg = (player == this.player) ? "🎉 WYGRAŁEŚ!" : "🤖 " + player.getName() + " wygrał!";
            showAlert(msg, Alert.AlertType.INFORMATION);
        }
    }

    private void nextTurn() {
        Player current = allPlayers.get(currentPlayerIndex);
        String[] result = Dice.rollTwoDice();

        if (current == player) {
            diceResult.setText("Twój rzut: " + result[0] + " + " + result[1]);
        } else {
            showAlert(current.getName() + " rzuca: " + result[0] + " + " + result[1], Alert.AlertType.INFORMATION);
        }

        handleThreats(current, result);
        handleBreeding(current, result);

        if (current != player) {
            autoExchange(current);
        }

        updateUI();

        if (checkVictory(current)) {
            String msg = (current == player) ? "🎉 WYGRAŁEŚ!" : "🤖 " + current.getName() + " wygrał!";
            showAlert(msg, Alert.AlertType.INFORMATION);
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % allPlayers.size();
    }

    private void handleThreats(Player p, String[] dice) {
        List<String> list = List.of(dice);

        if (list.contains("LIS")) {
            if (p.hasSmallDog()) {
                p.useSmallDog();
                bank.addAnimal(AnimalType.MALY_PIES, 1);
            } else {
                p.setAnimalCount(AnimalType.KROLIK, Math.min(1, p.getAnimalCount(AnimalType.KROLIK)));
            }
        }

        if (list.contains("WILK")) {
            if (p.hasBigDog()) {
                p.useBigDog();
                bank.addAnimal(AnimalType.DUZY_PIES, 1);
            } else {
                for (AnimalType t : AnimalType.values()) {
                    if (t != AnimalType.KROLIK && t != AnimalType.KON &&
                            t != AnimalType.MALY_PIES && t != AnimalType.DUZY_PIES) {
                        p.setAnimalCount(t, 0);
                    }
                }
            }
        }
    }

    private void handleBreeding(Player currentPlayer, String[] dice) {
        Map<AnimalType, Integer> rolledCount = new EnumMap<>(AnimalType.class);

        for (AnimalType type : AnimalType.values()) {
            rolledCount.put(type, 0);
        }

        for (String roll : dice) {
            try {
                AnimalType type = AnimalType.valueOf(roll);
                System.out.println(currentPlayer.getName() + " rolled: " + roll + " → " + type);
                if (type != AnimalType.MALY_PIES && type != AnimalType.DUZY_PIES) {
                    rolledCount.put(type, rolledCount.get(type) + 1);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("⚠ Unknown roll: " + roll);
            }
        }

        for (AnimalType type : AnimalType.values()) {
            if (type == AnimalType.MALY_PIES || type == AnimalType.DUZY_PIES) continue;

            int currentAnimals = currentPlayer.getAnimalCount(type);
            int rolledAnimals = rolledCount.get(type);
            int totalForBreeding = currentAnimals + rolledAnimals;
            int offspring = totalForBreeding / 2;

            System.out.println(currentPlayer.getName() + " - " + type + ": has " + currentAnimals +
                    ", rolled " + rolledAnimals + ", breeding total " + totalForBreeding +
                    ", offspring " + offspring);

            if (offspring > 0 && bank.removeAnimal(type, offspring)) {
                currentPlayer.addAnimal(type, offspring);
                System.out.println("Added " + offspring + " " + type + " to " + currentPlayer.getName());
            }
        }
    }

    private void autoExchange(Player p) {
        while (p.getAnimalCount(AnimalType.KROLIK) >= 6 && bank.removeAnimal(AnimalType.OWCA, 1)) {
            p.removeAnimal(AnimalType.KROLIK, 6);
            p.addAnimal(AnimalType.OWCA, 1);
        }
        while (p.getAnimalCount(AnimalType.OWCA) >= 2 && bank.removeAnimal(AnimalType.SWINIA, 1)) {
            p.removeAnimal(AnimalType.OWCA, 2);
            p.addAnimal(AnimalType.SWINIA, 1);
        }
        while (p.getAnimalCount(AnimalType.SWINIA) >= 3 && bank.removeAnimal(AnimalType.KROWA, 1)) {
            p.removeAnimal(AnimalType.SWINIA, 3);
            p.addAnimal(AnimalType.KROWA, 1);
        }
        while (p.getAnimalCount(AnimalType.KROWA) >= 2 && bank.removeAnimal(AnimalType.KON, 1)) {
            p.removeAnimal(AnimalType.KROWA, 2);
            p.addAnimal(AnimalType.KON, 1);
        }
    }

    private boolean checkVictory(Player player) {
        return player.getAnimalCount(AnimalType.KROLIK) > 0 &&
                player.getAnimalCount(AnimalType.OWCA) > 0 &&
                player.getAnimalCount(AnimalType.SWINIA) > 0 &&
                player.getAnimalCount(AnimalType.KROWA) > 0 &&
                player.getAnimalCount(AnimalType.KON) > 0;
    }

    private void updateUI() {
        playerResources.setText("Zasoby: " + player.getResources());
        bankResources.setText("Bank: " + bank.getResources());

        opponentsBox.getChildren().clear();
        opponentsBox.getChildren().add(new Label("Przeciwnicy:"));
        for (Player bot : opponents) {
            opponentsBox.getChildren().add(new Label(bot.getName() + ": " + bot.getResources()));
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
