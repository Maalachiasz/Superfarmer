package com.example.superfarmer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/superfarmer/setup-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 200);
        stage.setScene(scene);
        stage.setTitle("Superfarmer - Ustawienia gry");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
