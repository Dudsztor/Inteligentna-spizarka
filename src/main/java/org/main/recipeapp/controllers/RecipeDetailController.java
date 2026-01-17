package org.main.recipeapp.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.main.recipeapp.UserPreferences;
import org.main.recipeapp.dao.*;
import org.main.recipeapp.model.Recipe;
import org.main.recipeapp.model.RecipeIngredient;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailController {

    private static final List<RecipeDetailController> activeInstances = new ArrayList<>();

    @FXML private Label titleLabel;
    @FXML private ListView<RecipeIngredient> ingredientsList;
    @FXML private TextArea descriptionArea;
    @FXML private Button favoriteButton;

    private final IRecipeDao recipeDao = new RecipeDao();
    private final IShoppingListDao shoppingDao = new ShoppingListDao();

    private Recipe currentRecipe;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // odświeżanie otwartych okien
    public static void refreshAllOpenInstances() {
        for (RecipeDetailController controller : activeInstances) {
            controller.refreshView();
        }
    }

    @FXML
    public void initialize() {
        // dodawanie okna do listy aktywnych okien
        activeInstances.add(this);
    }

    // metoda do odświeżania widoku
    public void refreshView() {
        if (currentRecipe != null) {
            // pobieranie danych z bazy
            List<RecipeIngredient> ingredients = recipeDao.getIngredientsForRecipeId(currentRecipe.getId());

            // wpisujemy nowe dane do listy
            ingredientsList.getItems().setAll(ingredients);
            ingredientsList.refresh();
        }
    }

    // funkcja do przenoszenia danych z głównego okna do okienka recipe details
    public void setRecipeData(Recipe recipe) {
        this.currentRecipe = recipe;
        // tytuł pola recipedetail to nazwa dania
        titleLabel.setText(recipe.getTitle());
        // a tutaj opis
        descriptionArea.setText(recipe.getDescription());

        // sprawdzanie ulubionych
        boolean isFav = UserPreferences.isFavorite(recipe.getId());
        this.currentRecipe.setFavorite(isFav);
        updateFavoriteButtonIcon();

        // dodawanie składników do listy
        ingredientsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(RecipeIngredient item, boolean empty) {
                super.updateItem(item, empty);

                setStyle("-fx-background-color: transparent;");

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // kontener poziomy zeby przechowac skladnik
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    // dane
                    double neededQty = item.getQuantity();
                    double pantryQty = item.getQuantityInPantry();
                    double shoppingQty = item.getQuantityInShoppingList();

                    // system kolorów
                    String textColor = "#CF6679"; // CZERWONY

                    if (pantryQty >= neededQty) {
                        textColor = "#03DAC6"; // ZIELONY
                    } else if (shoppingQty >= neededQty) {
                        textColor = "#F1C40F"; // ŻÓŁTY
                    }

                    String text = "• " + item.getName() + " - " + item.getQuantity();
                    Label label = new Label(text);
                    label.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");

                    // takie do rozpychania zeby przycisk byl po prawej
                    Pane spacer = new Pane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button addButton = new Button("+ Koszyk");

                    if (pantryQty >= neededQty) {
                        addButton.setText("✓ W lodówce");
                        addButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #03DAC6; -fx-border-color: #03DAC6; -fx-border-radius: 5;");
                        addButton.setDisable(true);
                    } else if (shoppingQty >= neededQty) {
                        addButton.setText("✓ W koszyku");
                        addButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #F1C40F; -fx-border-color: #F1C40F; -fx-border-radius: 5;");
                        addButton.setDisable(true);
                    } else {
                        // przycisk dodajemy jak czegoś brakuje
                        addButton.setText("+ Koszyk");
                        addButton.setStyle("-fx-background-color: #3E3E55; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");

                        addButton.setOnAction(e -> {
                            boolean success = shoppingDao.addToShoppingList(item.getName(), item.getQuantity());
                            if (success) {
                                // bierzemy otwartą instancje kontrolera listy
                                ShoppingListController shoppingListController = ShoppingListController.getInstance();

                                // jesli okno jest otwarte to odswiezamy
                                if (shoppingListController != null) {
                                    shoppingListController.refreshList();
                                }
                                // odświeżamy wszystkie okna przepisów
                                refreshAllOpenInstances();
                            }
                        });
                    }
                    hbox.getChildren().addAll(label, spacer, addButton);
                    setGraphic(hbox);
                }
            }
        });
        refreshView();
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
            for (RecipeIngredient item : ingredientsList.getItems()) {
                writer.println("- " + item.getName() + " (" + item.getQuantity() + ")");
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

    // dodawanie do ulubionych
    @FXML
    private void onFavoriteToggle() {
        if (currentRecipe == null) return;

        boolean newState = !currentRecipe.isFavorite();
        currentRecipe.setFavorite(newState);

        // zapisywanie nowego stanu (w UserPreferences, nie w bazie)
        if (newState) {
            UserPreferences.addFavorite(currentRecipe.getId());
        } else {
            UserPreferences.removeFavorite(currentRecipe.getId());
        }

        // zmiana koloru
        updateFavoriteButtonIcon();

        // odświeżamy główną liste w main, żeby wskoczyło do góry
        if (mainController != null) {
            mainController.refreshAll();
        }
    }

    private void updateFavoriteButtonIcon() {
        if (currentRecipe.isFavorite()) {
            favoriteButton.setText("♡ Ulubione");
            favoriteButton.setStyle("-fx-background-color: #03dac6; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            favoriteButton.setText("♡ Dodaj do ulubionych");
            favoriteButton.setStyle("-fx-background-color: #3E3E55; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void onClose() {
        activeInstances.remove(this);
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}