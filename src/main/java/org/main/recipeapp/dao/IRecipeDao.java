package org.main.recipeapp.dao;

import org.main.recipeapp.model.MissingItemRecipe;
import org.main.recipeapp.model.Recipe;
import org.main.recipeapp.model.RecipeIngredient;

import java.util.List;

public interface IRecipeDao {
    void insertRecipe(Recipe recipe);
    List<Recipe> getAllRecipes();
    List<Recipe> searchRecipes(String query);
    void deleteRecipe(int recipeId);
    List<String> getAllIngredientNames();
    List<Recipe> getDoableRecipes();
    void insertRecipeIngredients(int recipeId, List<RecipeIngredient> ingredients);
    List<RecipeIngredient> getIngredientsForRecipeId(int recipeId);
    List<MissingItemRecipe> getAlmostDoableRecipes(int limit);
}