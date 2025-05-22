package com.example.superfarmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.*;
import java.util.Set;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Arrays;


import java.util.ArrayList;
import java.util.List;

public class GameController {

    private Player player;
    private Bank bank;
    private List<Player> opponents = new ArrayList<>();
    private List<Player> allPlayers = new ArrayList<>();
    private int currentPlayerIndex = 0;
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
    public void initialize() {
        player = new Player("Ty");
        bank = new Bank();

        opponents.add(new Player("Bot 1"));
        opponents.add(new Player("Bot 2"));

        allPlayers.add(player);
        allPlayers.addAll(opponents);

        giveAnimal.getItems().addAll(AnimalType.values());
        getAnimal.getItems().addAll(AnimalType.values());

        updateUI();
    }


    @FXML
    private void handleExchange() {
        if (!canExchange) {
            showAlert("Możesz wymienić tylko raz na turę – przed rzutem!", Alert.AlertType.WARNING);
            return;
        }

        int amount = 0;
        AnimalType give = null;
        AnimalType get = null;

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
    private Button rollBtn;

    @FXML
    private void handleRollDice() {
        nextTurn(); // tura gracza

        // tury botów
        for (int i = 1; i < allPlayers.size(); i++) {
            currentPlayerIndex = (currentPlayerIndex + 1) % allPlayers.size();
            Player bot = allPlayers.get(currentPlayerIndex);
            nextTurn();
            showAlert(bot.getName() + " zakończył turę.", Alert.AlertType.INFORMATION);
        }

        // wróć do gracza
        currentPlayerIndex = 0;
        canExchange = true; // reset po zakończeniu wszystkich tur

    }



    private void nextTurn() {
        System.out.println("Aktualny gracz: " + allPlayers.get(currentPlayerIndex).getName());
        Player current = allPlayers.get(currentPlayerIndex);
        String[] result = Dice.rollTwoDice();

        if (current == player) {
            diceResult.setText("Twój rzut: " + result[0] + " + " + result[1]);
        }

        System.out.println(current.getName() + " ma teraz: " + current.getResources());


        // 1. Zdarzenia specjalne (lis/wilk)
        handleThreats(current, result);

        // 2. Rozmnażanie
        handleBreeding(current, result);

        // 3. Wymiana (tylko boty automatycznie)
        if (current != player) {
            autoExchange(current);
        }

        updateUI();

        // 4. Sprawdzenie zwycięzcy
        if (checkVictory(current)) {
            String msg = (current == player) ? "🎉 WYGRAŁEŚ!" : "🤖 " + current.getName() + " wygrał!";
            showAlert(msg, Alert.AlertType.INFORMATION);
            return;
        }

        // 5. Następny gracz
        currentPlayerIndex = (currentPlayerIndex + 1) % allPlayers.size();
    }

    private void handleThreats(Player p, String[] dice) {
        List<String> list = List.of(dice);
        if (list.contains("LIS")) {
            if (p.hasSmallDog()) {
                p.useSmallDog();
                bank.addAnimal(AnimalType.MALY_PIES, 1);
            } else {
                int count = p.getAnimalCount(AnimalType.KROLIK);
                p.setAnimalCount(AnimalType.KROLIK, Math.min(1, count));
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
        System.out.println("Tura zakończona dla: " + allPlayers.get(currentPlayerIndex).getName());

    }

    private void handleBreeding(Player p, String[] dice) {
        // 1. Skopiuj aktualny stan gracza
        Map<AnimalType, Integer> breedingState = new EnumMap<>(AnimalType.class);
        for (AnimalType type : AnimalType.values()) {
            breedingState.put(type, p.getAnimalCount(type));
        }

        // 2. Dodaj do stanu wynik z kości (jako "tymczasowe osobniki")
        for (String roll : dice) {
            try {
                AnimalType type = AnimalType.valueOf(roll);
                if (type == AnimalType.MALY_PIES || type == AnimalType.DUZY_PIES) continue;

                breedingState.put(type, breedingState.getOrDefault(type, 0) + 1);
                System.out.println(breedingState.get(AnimalType.KROLIK));
            } catch (IllegalArgumentException ignored) {
                // ignorujemy LIS, WILK itp.
            }
        }

        // 3. Dla każdego typu — rozmnażanie
        for (AnimalType type : AnimalType.values()) {
            if (type == AnimalType.MALY_PIES || type == AnimalType.DUZY_PIES) continue;

            int total = breedingState.getOrDefault(type, 0);
            int offspring = total / 2;

            System.out.println("-> " + p.getName() + ": rozmnażanie " + type + " z rozszerzonym stanem (" + total + ") = " + offspring);

            if (offspring > 0 && bank.removeAnimal(type, offspring)) {
                p.addAnimal(type, offspring);
            }
        }
    }




    private void autoExchange(Player p) {
        // 6 królików = 1 owca
        while (p.getAnimalCount(AnimalType.KROLIK) >= 6 && bank.removeAnimal(AnimalType.OWCA, 1)) {
            p.removeAnimal(AnimalType.KROLIK, 6);
            p.addAnimal(AnimalType.OWCA, 1);
        }
        // 2 owce = 1 świnia
        while (p.getAnimalCount(AnimalType.OWCA) >= 2 && bank.removeAnimal(AnimalType.SWINIA, 1)) {
            p.removeAnimal(AnimalType.OWCA, 2);
            p.addAnimal(AnimalType.SWINIA, 1);
        }
        // 3 świnie = 1 krowa
        while (p.getAnimalCount(AnimalType.SWINIA) >= 3 && bank.removeAnimal(AnimalType.KROWA, 1)) {
            p.removeAnimal(AnimalType.SWINIA, 3);
            p.addAnimal(AnimalType.KROWA, 1);
        }
        // 2 krowy = 1 koń
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

    private String formatResources(Map<AnimalType, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (var entry : map.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(entry.getKey().name().toLowerCase()).append(": ").append(entry.getValue()).append("  ");
            }
        }
        return sb.toString();
    }


    private void updateUI() {
        System.out.println("Aktualizacja UI");

        playerResources.setText("Zasoby: " + formatResources(player.getResources()));

        bankResources.setText(bank.getResources().toString());

        // Aktualizacja zasobów przeciwników
        opponentsBox.getChildren().clear();
        opponentsBox.getChildren().add(new Label("Przeciwnicy:"));
        for (Player bot : opponents) {
            opponentsBox.getChildren().add(new Label(bot.getName() + ": " + formatResources(bot.getResources())));
        }

        //opponentsBox.getChildren().removeIf(node -> node instanceof Label && node != opponentsBox.getChildren().get(0));
        //for (Player bot : opponents) {
        //    Label label = new Label(bot.getName() + ": " + bot.getResources());
        //    opponentsBox.getChildren().add(label);
        //}

    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
