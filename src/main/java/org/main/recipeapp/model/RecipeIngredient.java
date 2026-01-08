package org.main.recipeapp.model;

public class RecipeIngredient {
    private Ingredient ingredient;
    private Double quantity;

    public RecipeIngredient(Ingredient ingredient, Double quantity) {
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public Ingredient getIngredient() { return ingredient; }
    public Double getQuantity() { return quantity; }
    public String getName() { return ingredient.getName(); }

    @Override
    public String toString() {
        return ingredient.getName() + " (" + quantity + ")";
    }
}