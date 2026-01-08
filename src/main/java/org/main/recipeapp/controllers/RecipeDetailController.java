package org.main.recipeapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Recipe;

import java.util.List;

public class RecipeDetailController {

    @FXML private Label titleLabel;
    @FXML private ListView<String> ingredientsList;
    @FXML private TextArea descriptionArea;

    private final RecipeDao recipeDao = new RecipeDao();

    // funkcja do przenoszenia danych z głównego okna do okienka recipe details
    public void setRecipeData(Recipe recipe) {
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

    @FXML
    private void onClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}