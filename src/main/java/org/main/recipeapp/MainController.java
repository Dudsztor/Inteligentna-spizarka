package org.main.recipeapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onAddRecipeClick() {
        try {
            //ładowanie drugiego okna
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add-recipe-view.fxml"));
            Parent root = fxmlLoader.load();

            //nowe okno
            Stage stage = new Stage();
            stage.setTitle("Dodaj nowy przepis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nie udało się otworzyć okna dodawania przepisu.");
        }
    }
}
