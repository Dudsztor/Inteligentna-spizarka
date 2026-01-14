package org.main.recipeapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.main.recipeapp.dao.IRecipeDao;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Recipe;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class RecipeDetailController {

    @FXML private Label titleLabel;
    @FXML private ListView<String> ingredientsList;
    @FXML private TextArea descriptionArea;

    private final IRecipeDao recipeDao = new RecipeDao();

    private Recipe currentRecipe;

    // funkcja do przenoszenia danych z głównego okna do okienka recipe details
    public void setRecipeData(Recipe recipe) {
        this.currentRecipe = recipe;
        // tytuł pola recipedetail to nazwa dania
        titleLabel.setText(recipe.getTitle());
        // a tutaj opis
        descriptionArea.setText(recipe.getDescription());

        // pobieranie składnika z bazy
        List<String> ingredients = recipeDao.getIngredientsForRecipe(recipe.getId());

        ingredientsList.getItems().setAll(ingredients);

        // dodawanie składników do listy
        ingredientsList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: transparent;");
                }
            }
        });
    }

    // kliknięcie w guzik
    @FXML
    private void onExportClick() {
        if (currentRecipe == null) return;

        // okno wyboru
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz przepis");
        fileChooser.setInitialFileName(currentRecipe.getTitle() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plik txt", "*.txt"));

        // okno że zapisano
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveToFile(file);
        }
    }

    // opis w środku pliku
    private void saveToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println("PRZEPIS: " + currentRecipe.getTitle().toUpperCase());
            writer.println("=================================");
            writer.println("");

            writer.println("SKŁADNIKI:");
            // pobieranie składników z listy
            for (String line : ingredientsList.getItems()) {
                writer.println(line);
            }

            writer.println("");
            writer.println("INSTRUKCJA:");
            writer.println(currentRecipe.getDescription());
            writer.println("");
            writer.println("=================================");

            showAlert("Sukces", "Zapisano przepis");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Błąd", "Nie udało się zapisać pliku: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}