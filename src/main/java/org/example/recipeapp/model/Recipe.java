package org.example.recipeapp.model;

import java.util.List;

public class Recipe {
    private String title;
    private List<Ingredient> ingredients;

    public Recipe(String title, List<Ingredient> ingredients) {
        this.title = title;
        this.ingredients = ingredients;
    }

    public String getTitle() {
        return title;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
