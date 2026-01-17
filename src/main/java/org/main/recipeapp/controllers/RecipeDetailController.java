package org.main.recipeapp.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.main.recipeapp.dao.IRecipeDao;
import org.main.recipeapp.dao.IShoppingListDao;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.dao.ShoppingListDao;
import org.main.recipeapp.model.Recipe;
import org.main.recipeapp.model.RecipeIngredient;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class RecipeDetailController {

    @FXML private Label titleLabel;
    @FXML private ListView<RecipeIngredient> ingredientsList;
    @FXML private TextArea descriptionArea;

    private final IRecipeDao recipeDao = new RecipeDao();
    private final IShoppingListDao shoppingDao = new ShoppingListDao();

    private Recipe currentRecipe;

    // funkcja do przenoszenia danych z głównego okna do okienka recipe details
    public void setRecipeData(Recipe recipe) {
        this.currentRecipe = recipe;
        // tytuł pola recipedetail to nazwa dania
        titleLabel.setText(recipe.getTitle());
        // a tutaj opis
        descriptionArea.setText(recipe.getDescription());

        // pobieranie składnika z bazy
        List<RecipeIngredient> ingredients = recipeDao.getIngredientsForRecipeId(recipe.getId());
        ingredientsList.getItems().setAll(ingredients);

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

                    String text = "• " + item.getName() + " - " + item.getQuantity();
                    Label label = new Label(text);
                    label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

                    // takie do rozpychania zeby przycisk byl po prawej
                    Pane spacer = new Pane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button addButton = new Button("+ Koszyk");
                    addButton.setStyle("-fx-background-color: #03DAC6; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand;");

                    addButton.setOnAction(e -> {
                        boolean success = shoppingDao.addToShoppingList(item.getName(), item.getQuantity());
                        if (success) {
                            addButton.setText("✓"); // klikniete
                            addButton.setStyle("-fx-background-color: #BB86FC; -fx-text-fill: white;"); // Fioletowy kolor sukcesu
                            addButton.setDisable(true); // blokujemy zeby nie klikać 100 razy

                            // bierzemy otwartą instancje kontrolera listy
                            ShoppingListController shoppingListController = ShoppingListController.getInstance();

                            // jesli okno jest otwarte to odswiezamy
                            if (shoppingListController != null) {
                                shoppingListController.refreshList();
                            }
                        }
                    });

                    hbox.getChildren().addAll(label, spacer, addButton);
                    setGraphic(hbox);
                }
            }
        });
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}