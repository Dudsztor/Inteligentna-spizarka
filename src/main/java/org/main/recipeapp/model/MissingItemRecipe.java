package org.main.recipeapp.model;

public class MissingItemRecipe {
    private Recipe recipe;
    private Ingredient missingIngredient;
    private double missingQuantity;

    public MissingItemRecipe(Recipe recipe, Ingredient missingIngredient, double missingQuantity) {
        this.recipe = recipe;
        this.missingIngredient = missingIngredient;
        this.missingQuantity = missingQuantity;
    }

    public Recipe getRecipe() { return recipe; }
    public Ingredient getMissingIngredient() { return missingIngredient; }
    public double getMissingQuantity() { return Math.round(missingQuantity * 100.0) / 100.0; }

    @Override
    public String toString() {
        return recipe.getTitle(); // Podstawowy widok
    }
}