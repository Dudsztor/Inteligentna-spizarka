package org.main.recipeapp.model;

public class RecipeIngredient {
    private Ingredient ingredient;
    private Double quantity;
    private double quantityInPantry = 0.0;
    private double quantityInShoppingList = 0.0;

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

    public void setQuantityInPantry(double qty) { this.quantityInPantry = qty; }
    public double getQuantityInPantry() { return quantityInPantry; }

    public void setQuantityInShoppingList(double qty) { this.quantityInShoppingList = qty; }
    public double getQuantityInShoppingList() { return quantityInShoppingList; }
}