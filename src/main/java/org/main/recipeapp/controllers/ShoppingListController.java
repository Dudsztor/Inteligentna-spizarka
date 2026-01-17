package org.main.recipeapp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.main.recipeapp.AutoCompleteListener;
import org.main.recipeapp.dao.IRecipeDao;
import org.main.recipeapp.dao.IShoppingListDao;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.dao.ShoppingListDao;
import org.main.recipeapp.model.PantryItem;

import java.util.List;

public class ShoppingListController {

    private static ShoppingListController instance;
    public static ShoppingListController getInstance() {
        return instance;
    }

    @FXML private ComboBox<String> itemInput;
    @FXML private TextField quantityInput;
    @FXML private ListView<PantryItem> shoppingListView;

    private final IShoppingListDao shoppingListDao = new ShoppingListDao();
    private final IRecipeDao recipeDao = new RecipeDao();
    private final ObservableList<PantryItem> itemsObservable = FXCollections.observableArrayList();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        instance = this;

        // konfiguracja listy
        shoppingListView.setItems(itemsObservable);
        refreshList();

        // combobox
        List<String> allIngredients = recipeDao.getAllIngredientNames();
        itemInput.getItems().addAll(allIngredients);
        new AutoCompleteListener<>(itemInput);

        // zeby tylko cyfry i kropke mozna bylo wpisac w ilosci
        quantityInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                quantityInput.setText(oldVal);
            }
        });
    }

    // przycisk dodania zakupu
    @FXML
    private void onAddItem() {
        String name = itemInput.getEditor().getText().trim();
        String qtyText = quantityInput.getText().trim();

        // odwołujemy funkcję jeśli nazwa albo ilość jest pusta
        if (name.isEmpty() || qtyText.isEmpty()) return;

        double qty = Double.parseDouble(qtyText);
        boolean success = shoppingListDao.addToShoppingList(name, qty);

        if (success) {
            itemInput.getEditor().clear();
            quantityInput.clear();
            refreshList();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nieznany składnik");
            alert.setContentText("Składnik nie istnieje w bazie");
            alert.showAndWait();
        }
    }

    // usuwanie składnika
    @FXML
    private void onDeleteItem() {
        PantryItem selected = shoppingListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            shoppingListDao.removeFromShoppingList(selected.getIngredient().getId());
            refreshList();
        }
    }

    public void refreshList() {
        itemsObservable.setAll(shoppingListDao.getShoppingList());
    }

    // zamykanie
    @FXML
    public void onClose() {
        instance = null; // zeby maincontroller wiedzial ze zamknelismy okno
        if (itemInput.getScene() != null) {
            itemInput.getScene().getWindow().hide();
        }
    }

    // kupowanie przedmiotu (przenoszenie do spiżarni)
    @FXML
    private void onBuyItem() {
        PantryItem selected = shoppingListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Nie wybrano produktu");
            alert.setContentText("Wybierz");
            alert.showAndWait();
            return;
        }
        shoppingListDao.buyItem(selected.getIngredient().getId(), selected.getQuantity());
        shoppingListDao.removeFromShoppingList(selected.getIngredient().getId());
        refreshList();

        mainController.refreshAll();
    }

    //do przywolywania okna na wierzch jakby
    public void focus() {
        if (itemInput != null && itemInput.getScene() != null) {
            Stage stage = (Stage) itemInput.getScene().getWindow();
            // jeśli zminimalizowane to przywroc
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            // daj na wierzch
            stage.toFront();
            stage.requestFocus();
        }
    }


}