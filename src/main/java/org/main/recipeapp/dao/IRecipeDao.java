package org.main.recipeapp.dao;

import org.main.recipeapp.model.Recipe;
import java.util.List;

public interface IRecipeDao {
    void insertRecipe(Recipe recipe);
    List<Recipe> getAllRecipes();
    List<Recipe> searchRecipes(String query);
    void deleteRecipe(int recipeId);
    List<String> getIngredientsForRecipe(int recipeId);
    List<String> getAllIngredientNames();
    List<Recipe> getDoableRecipes();
}