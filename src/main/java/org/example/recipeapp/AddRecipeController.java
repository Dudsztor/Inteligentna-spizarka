package org.example.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AddRecipeController {
    @FXML private TextField ingredientInput;
    @FXML private ListView<String> ingredientsListView;

    private final ObservableList<String> tempIngredients = FXCollections.observableArrayList();

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
        System.out.println("Zapisano przepis!");
    }
}