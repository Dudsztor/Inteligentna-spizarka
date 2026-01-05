package org.main.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        refreshAll();

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

            //odświeża wszystko
            refreshAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // refreshowanie checkboxów po lewej
    private void refreshIngredientsCheckboxes() {
        //czyści pudełko
        ingredientsContainer.getChildren().clear();
        List<String> ingredientNames = recipeDao.getAllIngredientNames();
        //wypełnia pudełko
        for (String name : ingredientNames) {
            CheckBox checkBox = new CheckBox(name);
            ingredientsContainer.getChildren().add(checkBox);
        }
    }

    // refreshowanie przepisów
    private void refreshRecipesList() {
        String currentQuery = searchField.getText();
        loadRecipes(currentQuery);
    }

    private void refreshAll(){
        //odświeżenie kolumny prawej
        refreshRecipesList();
        //odświeżenie kolumny lewej
        refreshIngredientsCheckboxes();
    }

    //usuwanie przepisu
    @FXML
    protected void onDeleteRecipeClick() {
        Recipe selectedRecipe = allRecipesList.getSelectionModel().getSelectedItem();

        //sprawdza czy wybraliśmy przepis
        if (selectedRecipe == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uwaga");
            alert.setHeaderText("Błąd");
            alert.setContentText("Nie wybrano przepisu");
            alert.showAndWait();
            return;
        }

        //upewnienie się że chce się usunąć przepis
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Uwaga");
        confirmAlert.setHeaderText("Usuwanie przepisu");
        confirmAlert.setContentText("Czy na pewno chcesz usunąć ten przepis?");

        //potwierdzenie usunięcia
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recipeDao.deleteRecipe(selectedRecipe.getId());

                //odświeżenie widoku
                refreshAll();
            }
        });
    }
}