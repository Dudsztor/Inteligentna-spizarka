package org.main.recipeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.main.recipeapp.dao.RecipeDao;
import org.main.recipeapp.model.Recipe;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private ListView<Recipe> recipesList;
    private RecipeDao recipeDao = new RecipeDao();

    @FXML
    public void initialize(){
        //refreshRecipesList();
    }

    //odświeżanie listy przepisów
    private void refreshRecipesList(){
        List<Recipe> recipesFromDB = recipeDao.getAllRecipes();
        ObservableList<Recipe> observableList = FXCollections.observableList(recipesFromDB);
        recipesList.setItems(observableList);
    }

    @FXML
    protected void onAddRecipeClick() {
        try {
            //ładowanie drugiego okna
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add-recipe-view.fxml"));
            Parent root = fxmlLoader.load();

            //nowe okno
            Stage stage = new Stage();
            stage.setTitle("Dodaj nowy przepis");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshRecipesList();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nie udało się otworzyć okna dodawania przepisu.");
        }
    }
}
