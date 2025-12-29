package org.main.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeController {
    @FXML private TextField ingredientInput;
    @FXML private ListView<String> ingredientsListView;
    @FXML private TextField titleField;
    @FXML private TextArea descField;

    private final ObservableList<String> tempIngredients = FXCollections.observableArrayList();

    private RecipeDao recipeDao = new RecipeDao();

    @FXML
    public void initialize() {
        ingredientsListView.setItems(tempIngredients);

        //usuwanie składnika
        ingredientsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = ingredientsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tempIngredients.remove(selected);
                }
            }
        });
    }

    //dodawanie nowego składnika
    @FXML
    private void onAddIngredientToList() {
        String name = ingredientInput.getText().trim();
        if (!name.isEmpty() && !tempIngredients.contains(name)) {
            tempIngredients.add(name);
            ingredientInput.requestFocus();
        }
    }

    //zapisywanie przepisu
    @FXML
    private void onSaveRecipe() {
        String title = titleField.getText().trim(); // pobieranie tekstu z pola
        String description = descField.getText().trim();     // pobieranie opisu z pola

        List<Ingredient> ingredientsList = new ArrayList<>();

        for (String ingredientName : tempIngredients) {
            // Tworzymy obiekt Ingredient używając jego konstruktora
            ingredientsList.add(new Ingredient(ingredientName));
        }

        Recipe newRecipe = new Recipe(title, description, ingredientsList);

        // Zapisujemy do bazy zamiast do pamięci RAM
        recipeDao.insertRecipe(newRecipe);

        System.out.println("Zapisano przepis w bazie danych SQLite!");

        // Opcjonalnie: wyczyść pola formularza po zapisie
        tempIngredients.clear();
        ingredientInput.clear();

        //zamykanie okna po zapisie
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
