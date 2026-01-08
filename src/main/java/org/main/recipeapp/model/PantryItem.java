package org.main.recipeapp.model;

public class PantryItem {
    private Ingredient ingredient;
    private Double quantity;

    public PantryItem(Ingredient ingredient, Double quantity) {
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public Ingredient getIngredient() { return ingredient; }
    public Double getQuantity() { return quantity; }

    // formatowanie do wierszy, np. wpisując Ananas i 2 to zmieni na Ananas (2)
    @Override
    public String toString() {
        if (quantity == 0.0) {
            return ingredient.getName();
        }
        return ingredient.getName() + " (" + quantity + ")";
    }
}