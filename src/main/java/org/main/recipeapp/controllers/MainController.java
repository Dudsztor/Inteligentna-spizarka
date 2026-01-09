package org.main.recipeapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.main.recipeapp.AutoCompleteListener;
import org.main.recipeapp.dao.IPantryDao;
import org.main.recipeapp.dao.IRecipeDao;
import org.main.recipeapp.dao.PantryDao;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.PantryItem;
import org.main.recipeapp.model.Recipe;

import java.io.IOException;
import java.util.List;

public class MainController {

    // --- LEWA KOLUMNA (Spiżarnia) ---
    public VBox pantryContainer;
    @FXML private ListView<PantryItem> pantryListView;
    @FXML private ComboBox<String> pantryInput;
    @FXML private TextField quantityInput;

    // --- ŚRODKOWA KOLUMNA (Smart Lista) ---
    @FXML private ListView<Recipe> smartRecipeList;

    // --- PRAWA KOLUMNA (Wyszukiwarka) ---
    @FXML private TextField searchField;
    @FXML private ListView<Recipe> allRecipesList;

    private IRecipeDao recipeDao;
    private IPantryDao pantryDao;

    private ObservableList<PantryItem> pantryObservable = FXCollections.observableArrayList();
    private ObservableList<Recipe> allRecipesObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.recipeDao = new RecipeDao();
        this.pantryDao = new PantryDao();

        // konfiguracje lewej kolumny
        List<String> validIngredients = recipeDao.getAllIngredientNames();
        pantryInput.getItems().addAll(validIngredients);
        new AutoCompleteListener<>(pantryInput);
        pantryListView.setItems(pantryObservable);
        setupPantryCellFactory();

        quantityInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantityInput.setText(oldValue);
            }
        });

        // konfiguracja prawej kolumny (baza wszystkich przepisów)
        allRecipesList.setItems(allRecipesObservable);
        loadRecipes(""); // Załaduj wszystko na start

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadRecipes(newValue);
        });

        setupRecipeCellFactory(allRecipesList);
        refreshSmartList();

        allRecipesList.setItems(allRecipesObservable);
        setupRecipeCellFactory(allRecipesList); // metoda do pokazywania komórek

        // sprawdzanie czy się klikło dwa razy na przepis
        allRecipesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Recipe selected = allRecipesList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openRecipeDetails(selected);
                }
            }
        });
        refreshAll();
    }

    @FXML
    protected void onShoppingListClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/main/recipeapp/shopping-list-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            ShoppingListController shoppingListController = fxmlLoader.getController();
            shoppingListController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Lista Zakupów");
            stage.setScene(scene);
            stage.showAndWait();
            refreshAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onAddRecipeClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/main/recipeapp/add-recipe-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Dodaj nowy przepis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void refreshAll(){
        //odświeżenie kolumny prawej
        refreshRecipesList();
        //odświeżenie kolumny lewej
        refreshPantryList();
        //odswiezanie srodkowej
        refreshSmartList();
    }
    // =====================================================================================================
    // LODÓWKA - LEWA STRONA

    // refreshowanie listy po lewej
    private void refreshPantryList() {
        List<PantryItem> myItems = pantryDao.getPantryItems();
        pantryObservable.setAll(myItems);
    }

    //dodawanie składnika - przycisk
    @FXML
    private void onAddPantryItem() {
        double quantity = 0.0;
        // pobieranie napisu
        String name = pantryInput.getEditor().getText();
        String quantityText = quantityInput.getText().trim();

        // jeśli napis nie jest pusty ani nie jest spacją
        if (name != null && !name.trim().isEmpty() && quantityText != null && !quantityText.trim().isEmpty()) {
            quantity = Double.parseDouble(quantityText);

            // sprawdzamy czy się dodało
            boolean success = pantryDao.addIngredientToPantryStrict(name, quantity);

            // jeśli się dodało to czyścimy pola
            if (success) {
                pantryInput.getEditor().clear();
                pantryInput.setValue(null);
                quantityInput.clear();
                refreshAll();
            } else {
                // jeśli jest błąd to jest błąd
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Nieznany składnik");
                alert.setContentText("Składnik '" + name + "' nie znajduje się w naszej bazie danych.\nWybierz składnik z listy podpowiedzi.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Brak danych");
            alert.setContentText("Podaj liczbę");
            alert.showAndWait();
        }
    }

    @FXML
    private void onDeletePantryItemClick() {
        PantryItem selectedItem = pantryListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie wybrano składnika");
            alert.setContentText("Wybierz składnik");
            alert.showAndWait();
            return;
        }

        // Usuwamy z bazy
        pantryDao.removeFromPantry(selectedItem.getIngredient().getId());

        // Odświeżamy widok
        refreshAll();
    }

    private void setupPantryCellFactory() {
        pantryListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PantryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // wypisywanie składników w lodówce
                    double qty = item.getQuantity();
                    String qtyString = String.format("%.2f", qty);
                    setText(item.getIngredient().getName() + " (" + qtyString + ")");
                }
            }
        });
    }

    // =====================================================================================================
    // SMART LISTA - ŚRODEK
    private void refreshSmartList() {
        List<Recipe> doable = recipeDao.getDoableRecipes();
        smartRecipeList.getItems().setAll(doable);
        setupRecipeCellFactory(smartRecipeList);
    }

    // klikanie gotowania
    @FXML
    private void onCookRecipeClick() {
        // pobieramy składnik z listy
        Recipe selectedRecipe = smartRecipeList.getSelectionModel().getSelectedItem();

        if (selectedRecipe == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie wybrano przepisu");
            alert.setContentText("Wybierz przepis");
            alert.showAndWait();
            return;
        }

        // chcemy potwierdzenie
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Ugotować " + selectedRecipe.getTitle() + "?");
        confirm.setContentText("Składniki zostaną odjęte z Twojej spiżarni.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                // gotujemy
                pantryDao.cookRecipe(selectedRecipe);

                // odświeżamy wszystko
                refreshAll();

                // sukces 🎆🎆🎆
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Ugotowane");
                success.setHeaderText("Smacznego");
                success.setContentText("Składniki zostały zaktualizowane.");
                success.show();
            }
        });
    }

    // =====================================================================================================
    // PRZEPISY - PRAWA STRONA

    // refreshowanie przepisów
    private void refreshRecipesList() {
        String currentQuery = searchField.getText();
        loadRecipes(currentQuery);
    }

    // otwieranie przepisu
    private void openRecipeDetails(Recipe recipe) {
        try {
            //otwiera plik fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/main/recipeapp/recipe-detail-view.fxml"));
            Parent root = loader.load();

            // pobieramy kontroler
            RecipeDetailController controller = loader.getController();
            // pokazujemy przepis kontrolerowi
            controller.setRecipeData(recipe);

            Stage stage = new Stage();
            stage.setTitle(recipe.getTitle());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nie udało się otworzyć szczegółów przepisu.");
        }
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

    // metoda ustawiająca wygląd komórki dla dowolnej listy przepisów
    private void setupRecipeCellFactory(ListView<Recipe> listView) {
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

    private void loadRecipes(String query) {
        allRecipesObservable.clear();
        if (query == null || query.isEmpty()) {
            allRecipesObservable.addAll(recipeDao.getAllRecipes());
        } else {
            allRecipesObservable.addAll(recipeDao.searchRecipes(query));
        }
    }
}