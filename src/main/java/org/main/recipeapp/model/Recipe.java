package org.main.recipeapp.model;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private String description;
    private List<RecipeIngredient> ingredients;
    private boolean isFavorite;

    //konstruktor dla nowych przepisów
    public Recipe(String title, String description, List<RecipeIngredient> ingredients) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    //konstruktor dla przepisów pobieranych z bazy
    public Recipe(int id, String title, String description, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
    public boolean isFavorite() { return isFavorite; }
}
