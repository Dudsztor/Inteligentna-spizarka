package org.main.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Recipe;

import java.io.IOException;
import java.util.List;

public class MainController {

    // --- LEWA KOLUMNA (Spiżarnia) ---
    public VBox ingredientsContainer;

    // --- ŚRODKOWA KOLUMNA (Smart Lista) ---
    @FXML private ListView<Recipe> smartRecipeList;

    // --- PRAWA KOLUMNA (Wyszukiwarka) ---
    @FXML private TextField searchField;
    @FXML private ListView<Recipe> allRecipesList;

    private RecipeDao recipeDao = new RecipeDao();
    private ObservableList<Recipe> allRecipesObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // konfiguracje lewej kolumny
        loadIngredientsCheckboxes();

        // konfiguracja prawej kolumny (baza wszystkich przepisów)
        allRecipesList.setItems(allRecipesObservable);
        loadRecipes(""); // Załaduj wszystko na start

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadRecipes(newValue);
        });

        // ladne wyświetlanie nazw
        setupListViewCellFactory(allRecipesList);

        // tutaj do zrobienia srodkowa kolumna z dostepnymi przepisami
        // setupListViewCellFactory(smartRecipeList);
    }

    private void loadRecipes(String query) {
        allRecipesObservable.clear();
        if (query == null || query.isEmpty()) {
            allRecipesObservable.addAll(recipeDao.getAllRecipes());
        } else {
            allRecipesObservable.addAll(recipeDao.searchRecipes(query));
        }
    }

    // metoda ustawiająca wygląd komórki dla dowolnej listy przepisów
    private void setupListViewCellFactory(ListView<Recipe> listView) {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Recipe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
    }

    @FXML
    protected void onAddRecipeClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/UI/recipeapp/add-recipe-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Dodaj nowy przepis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Odśwież prawą kolumnę po dodaniu
            loadRecipes(searchField.getText());
            // odśwież listę składników po lewej
            loadIngredientsCheckboxes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadIngredientsCheckboxes() {
        ingredientsContainer.getChildren().clear();
        List<String> ingredientNames = recipeDao.getAllIngredientNames();
        for (String name : ingredientNames) {
            CheckBox checkBox = new CheckBox(name);
            ingredientsContainer.getChildren().add(checkBox);
        }
    }
}