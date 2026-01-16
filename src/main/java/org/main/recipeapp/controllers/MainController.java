package org.main.recipeapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.main.recipeapp.AutoCompleteListener;
import org.main.recipeapp.Main;
import org.main.recipeapp.dao.*;
import org.main.recipeapp.model.MissingItemRecipe;
import org.main.recipeapp.model.PantryItem;
import org.main.recipeapp.model.Recipe;
import org.main.recipeapp.model.TimerBox;

import java.io.IOException;
import java.util.List;

import static java.lang.Integer.parseInt;

public class MainController {

    // --- LEWA KOLUMNA (Spiżarnia) ---
    public VBox pantryContainer;
    @FXML private ListView<PantryItem> pantryListView;
    @FXML private ComboBox<String> pantryInput;
    @FXML private TextField quantityInput;

    // --- ŚRODKOWA KOLUMNA (Smart Lista) ---
    @FXML private ListView<Recipe> smartRecipeList;

    // --- SRODKOWA DOLNA
    @FXML private ListView<MissingItemRecipe> almostDoableList;
    private IShoppingListDao shoppingDao;

    // --- PRAWA KOLUMNA (Wyszukiwarka) ---
    @FXML private TextField searchField;
    @FXML private ListView<Recipe> allRecipesList;

    // --- BARDZIEJ PRAWA KOLUMNA (Zegary) ---
    @FXML private VBox timersContainer;
    @FXML private TextField timerNameInput;
    @FXML private TextField timerMinutesInput;
    @FXML private TextField timerSecondsInput;

    private IRecipeDao recipeDao;
    private IPantryDao pantryDao;

    private ObservableList<PantryItem> pantryObservable = FXCollections.observableArrayList();
    private ObservableList<Recipe> allRecipesObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.recipeDao = new RecipeDao();
        this.pantryDao = new PantryDao();
        this.shoppingDao = new ShoppingListDao();

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
        // można tylko wpisywać liczby
        timerMinutesInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\d*)?")) {
                timerMinutesInput.setText(oldValue);
            }
        });
        timerSecondsInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\d*)?")) {
                timerSecondsInput.setText(oldValue);
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

        smartRecipeList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Recipe selected = smartRecipeList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openRecipeDetails(selected);
                }
            }
        });

        // --- KONFIGURACJA LISTY "BRAKUJE 1 SKŁADNIKA" ---
        setupAlmostDoableCellFactory();

        almostDoableList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MissingItemRecipe selected = almostDoableList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // wyciągamy czysty przepis z wrappera i otwieramy okno szczegółów
                    openRecipeDetails(selected.getRecipe());
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
            Main.setAppIcon(stage);
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
            Main.setAppIcon(stage);
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
        //dolna srodkowa
        refreshAlmostDoableList();
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
                showAlert("Nieznany składnik", "Składnik " + name + " nie znajduje się w bazie danych.");
            }
        } else {
            showAlert("Uwaga","Podaj liczbę");
        }
    }

    @FXML
    private void onDeletePantryItemClick() {
        PantryItem selectedItem = pantryListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showAlert("Uwaga","Nie wybrano składnika");
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
            showAlert("Uwaga", "Nie wybrano przepisu");
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
                showAlert("Brawo", "Ugotowano!");
            }
        });
    }

    // SRODEK DOL

    private void refreshAlmostDoableList() {
        // np 5 propozycji z RecipeDao
        List<MissingItemRecipe> almostDoable = recipeDao.getAlmostDoableRecipes(5);
        if (almostDoableList != null) {
            almostDoableList.getItems().setAll(almostDoable);
        }
    }

    private void setupAlmostDoableCellFactory() {
        almostDoableList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MissingItemRecipe item, boolean empty) {
                super.updateItem(item, empty);

                // czyszczone tło samej komórki listy, żeby była przezroczysta
                setStyle("-fx-background-color: transparent; -fx-padding: 5px;");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // glowny kontener
                    VBox container = new VBox(3); // odstęp 3px między tytułem a brakiem
                    container.setAlignment(Pos.CENTER_LEFT);

                    // tytul przepisu
                    Label titleLabel = new Label(item.getRecipe().getTitle());
                    titleLabel.setStyle("-fx-text-fill: #BB86FC; -fx-font-weight: bold; -fx-font-size: 14px;");

                    // info o braku
                    String missingText = "Brakuje: " + item.getMissingIngredient().getName() +
                            " (" + item.getMissingQuantity() + ")";
                    Label missingLabel = new Label(missingText);
                    missingLabel.setStyle("-fx-text-fill: #CF6679; -fx-font-size: 11px; -fx-font-style: italic;");

                    // Dodajemy etykiety do kontenera
                    container.getChildren().addAll(titleLabel, missingLabel);

                    setGraphic(container);
                }
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
            Main.setAppIcon(stage);
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
            showAlert("Uwaga", "Nie wybrano przepisu");
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

    // =====================================================================================================
    // ZEGARY - BARDZIEJ PRAWA STRONA

    @FXML
    private void onAddTimerClick() {

        String name = timerNameInput.getText().trim();

        // pobieramy tekst
        String minText = timerMinutesInput.getText().trim();
        String secText = timerSecondsInput.getText().trim();

        if (name.isEmpty()) return;

        // zmienia wpisany string na liczbe - jak sie wpisało coś innego to wywala błąd
        int minutes = minText.isEmpty() ? 0 : parseInt(minText);
        int seconds = secText.isEmpty() ? 0 : parseInt(secText);

        if (minutes == 0 && seconds == 0) return;

        // przeliczanie na sekundy
        int totalSeconds = (minutes * 60) + seconds;

        // timer box
        TimerBox newTimer = new TimerBox(name, totalSeconds, timersContainer);
        timersContainer.getChildren().add(newTimer);

        // czyszczenie pól
        timerNameInput.clear();
        timerMinutesInput.clear();
        timerSecondsInput.clear();
    }

    private void loadRecipes(String query) {
        allRecipesObservable.clear();
        if (query == null || query.isEmpty()) {
            allRecipesObservable.addAll(recipeDao.getAllRecipes());
        } else {
            allRecipesObservable.addAll(recipeDao.searchRecipes(query));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}