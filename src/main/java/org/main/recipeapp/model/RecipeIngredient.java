package org.main.recipeapp.model;

public class RecipeIngredient {
    private Ingredient ingredient;
    private String quantity;

    public RecipeIngredient(Ingredient ingredient, String quantity) {
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public Ingredient getIngredient() { return ingredient; }
    public String getQuantity() { return quantity; }
    public String getName() { return ingredient.getName(); }

    @Override
    public String toString() {
        return ingredient.getName() + " (" + quantity + ")";
    }
}