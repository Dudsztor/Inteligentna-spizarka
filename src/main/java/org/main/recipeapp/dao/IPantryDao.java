package org.main.recipeapp.dao;

import org.main.recipeapp.model.PantryItem;
import org.main.recipeapp.model.Recipe;

import java.util.List;

public interface IPantryDao {
    List<PantryItem> getPantryItems();
    boolean addIngredientToPantryStrict(String name, Double quantity);
    void removeFromPantry(int ingredientId);
    void cookRecipe(Recipe recipe);
}