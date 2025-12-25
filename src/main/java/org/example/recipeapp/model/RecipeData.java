package org.example.recipeapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.Set;

public class RecipeData {
    private static ObservableList<Recipe> recipes = FXCollections.observableArrayList();
    private static Set<String> allIngredients = new HashSet<>();

    public static ObservableList<Recipe> getRecipes() { return recipes; }
    public static Set<String> getAllIngredients() { return allIngredients; }

    public static void addRecipe(Recipe recipe) {
        recipes.add(recipe);
        for (Ingredient ingredient : recipe.getIngredients()) {
            allIngredients.add(ingredient.getName());
        }
    }
}
