package org.example.recipeapp.model;

import java.util.List;

public class Recipe {
    private String title;
    private String description;
    private List<Ingredient> ingredients;

    public Recipe(String title, String description, List<Ingredient> ingredients) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Ingredient> getIngredients() { return ingredients; }
}
