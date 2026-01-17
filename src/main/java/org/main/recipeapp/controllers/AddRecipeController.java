package org.main.recipeapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.main.recipeapp.AutoCompleteListener;
import org.main.recipeapp.dao.IRecipeDao;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.RecipeIngredient;
import org.main.recipeapp.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeController {

    private static AddRecipeController instance;
    public static AddRecipeController getInstance() {
        return instance;
    }

    @FXML private ComboBox<String> ingredientInput;
    @FXML private ListView<RecipeIngredient> ingredientsListView;
    @FXML private TextField quantityInput;
    @FXML private TextField titleField;
    @FXML private TextArea descField;

    private final ObservableList<RecipeIngredient> tempIngredients = FXCollections.observableArrayList();
    private final IRecipeDao recipeDao = new RecipeDao();

    private MainController mainController;

    @FXML
    public void initialize() {

        instance = this;

        ingredientsListView.setItems(tempIngredients);

        List<String> dbIngredients = recipeDao.getAllIngredientNames();
        ingredientInput.getItems().addAll(dbIngredients);


        ingredientInput.setEditable(true);
        ingredientInput.setVisibleRowCount(5);
        ingredientInput.setPromptText("Wybierz składnik (np. makaron Spaghetti)");

        new AutoCompleteListener<>(ingredientInput);

        //usuwanie składnika
        ingredientsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                RecipeIngredient selected = ingredientsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tempIngredients.remove(selected);
                }
            }
        });

        // sprawdzanie czy wpisany tekst to tylko liczba z kropką
        quantityInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantityInput.setText(oldValue);
            }
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    //dodawanie nowego składnika
    @FXML
    private void onAddIngredientToList() {
        double quantity = 0.0;
        // pobieramy nazwę z ComboBoxa i ilość z pola tekstowego
        String name = ingredientInput.getEditor().getText().trim();
        String quantityText = quantityInput.getText().trim();

        if (!name.isEmpty()) {

            quantity = Double.parseDouble(quantityText);

            // filtrowanie naszej listy składników i czy tam istnieje składnik o nazwie name
            String dbName = ingredientInput.getItems().stream()
                    .filter(existingName -> existingName.equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);

            // jeśli nie znalazło w bazie naszego składnika
            if (dbName == null) {
                showAlert("Nieznany składnik", "Składnik '" + name + "' nie istnieje w bazie danych.\nWybierz poprawny składnik z listy.");
                return;
            }

            // tworzymy skladnik z sama nazwa
            Ingredient pureIngredient = new Ingredient(dbName);

            // twprzymy obiekt laczacy nazwe z iloscia
            RecipeIngredient item = new RecipeIngredient(pureIngredient, quantity);

            // sprawdzamy czy juz jest dodany
            boolean exists = tempIngredients.stream()
                    .anyMatch(i -> i.getName().equalsIgnoreCase(name));

            if (!exists) {
                tempIngredients.add(item); // dodajemy do listy widocznej na ekranie

                // czyscimy pola
                ingredientInput.getEditor().clear();
                ingredientInput.setValue(null);
                quantityInput.clear();

                // ustawiamy kursor z powrotem na nazwę
                ingredientInput.requestFocus();
            }
        }
    }

    //zapisywanie przepisu
    @FXML
    private void onSaveRecipe() {
        String title = titleField.getText().trim(); // pobieranie tekstu z pola
        String description = descField.getText().trim();     // pobieranie opisu z pola

        if (title.isEmpty()) {
            showAlert("Brak tytułu", "Podaj nazwę przepisu.");
            return;
        }

        if (tempIngredients.isEmpty()) {
            showAlert("Brak składników", "Dodaj przynajmniej jeden składnik.");
            return;
        }

        List<RecipeIngredient> finalIngredients = new ArrayList<>(tempIngredients);

        Recipe newRecipe = new Recipe(title, description, finalIngredients);

        recipeDao.insertRecipe(newRecipe);

        // Zamknięcie okna
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uwaga");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //do przywolywania okna na wierzch jak w shoppinglist
    public void focus() {
        if (ingredientInput != null && ingredientInput.getScene() != null) {
            Stage stage = (Stage) ingredientInput.getScene().getWindow();
            // jeśli zminimalizowane to przywroc
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            // daj na wierzch
            stage.toFront();
            stage.requestFocus();
        }
    }

    // zamykanie
    @FXML
    public void onClose() {
        instance = null; // zeby maincontroller wiedzial ze zamknelismy okno
        if (ingredientInput.getScene() != null) {
            ingredientInput.getScene().getWindow().hide();
        }
    }

}
