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
    @FXML private ComboBox<String> pantryInput;
    @FXML private TextField quantityInput;

    // --- ŚRODKOWA KOLUMNA (Smart Lista) ---
    @FXML private ListView<Recipe> smartRecipeList;

    // --- PRAWA KOLUMNA (Wyszukiwarka) ---
    @FXML private TextField searchField;
    @FXML private ListView<Recipe> allRecipesList;

    private IRecipeDao recipeDao;
    private IPantryDao pantryDao;

    private ObservableList<Recipe> allRecipesObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.recipeDao = new RecipeDao();
        this.pantryDao = new PantryDao();

        // konfiguracje lewej kolumny
        List<String> validIngredients = recipeDao.getAllIngredientNames();
        pantryInput.getItems().addAll(validIngredients);
        new AutoCompleteListener<>(pantryInput);

        // pozwolenie na cyfry i jedną kropkę -> jeśli nowy napis zawiera literę to zostawia stary napis
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

        // ladne wyświetlanie nazw
        setupListViewCellFactory(allRecipesList);

        //lewa kolumna
        refreshSmartList();

        allRecipesList.setItems(allRecipesObservable);
        setupListViewCellFactory(allRecipesList); // metoda do pokazywania komórek

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

    private void refreshSmartList() {
        //pobieramy przepis
        List<Recipe> doable = recipeDao.getDoableRecipes();

        //wyswietlamy
        smartRecipeList.getItems().setAll(doable);

        //tytul zeby sie wyswietlal
        setupListViewCellFactory(smartRecipeList);
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/main/recipeapp/add-recipe-view.fxml"));
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
    private void refreshPantryList() {
        pantryContainer.getChildren().clear();

        // pobieramy listę obiektów z pantryDao - nazwa + ilość
        List<PantryItem> myItems = pantryDao.getPantryItems();

        // tworzenie listy pod spodem - napis po lewej, przycisk X po prawej
        for (PantryItem item : myItems) {
            HBox row = new HBox();
            row.setSpacing(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 5; -fx-background-color: #363649; -fx-background-radius: 5;");

            // tekst składnika
            Label infoLabel = new Label(item.toString());
            infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            infoLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(infoLabel, Priority.ALWAYS);

            // przycisk usuwania
            Button removeBtn = new Button("X");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF5555; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
            removeBtn.setOnAction(e -> {
                int idToDelete = item.getIngredient().getId();
                pantryDao.removeFromPantry(idToDelete);

                refreshAll();
            });

            // dodaje wiersz z nazwą i przyciskiem
            row.getChildren().addAll(infoLabel, removeBtn);
            pantryContainer.getChildren().add(row);
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
        refreshPantryList();
        //odswiezanie srodkowej
        refreshSmartList();
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

    //dodawanie składnika - przycisk
    @FXML
    private void onAddPantryItem() {
        // pobieranie napisu
        String name = pantryInput.getEditor().getText();
        String quantity = quantityInput.getText();

        // jeśli napis nie jest pusty ani nie jest spacją
        if (name != null && !name.trim().isEmpty()) {
            // jeśli ilość składników jest pusta to zastępuje ---
            if (quantity == null || quantity.trim().isEmpty()) quantity = "---";

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
        }
    }

    // otwieranie składnika
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
}