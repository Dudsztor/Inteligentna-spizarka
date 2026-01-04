package org.main.recipeapp.model;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private String description;
    private List<Ingredient> ingredients;

    //konstruktor dla nowych przepisów
    public Recipe(String title, String description, List<Ingredient> ingredients) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    //konstruktor dla przepisów pobieranych z bazy
    public Recipe(int id, String title, String description, List<Ingredient> ingredients) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Ingredient> getIngredients() { return ingredients; }
}
