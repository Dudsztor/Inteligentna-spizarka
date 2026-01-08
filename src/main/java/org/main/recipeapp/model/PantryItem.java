package org.main.recipeapp.model;

public class PantryItem {
    private Ingredient ingredient;
    private String quantity;

    public PantryItem(Ingredient ingredient, String quantity) {
        this.ingredient = ingredient;
        this.quantity = quantity;
    }

    public Ingredient getIngredient() { return ingredient; }
    public String getQuantity() { return quantity; }

    // formatowanie do wierszy, np. wpisując Ananas i 2 to zmieni na Ananas (2)
    @Override
    public String toString() {
        if (quantity == null || quantity.isEmpty()) {
            return ingredient.getName();
        }
        return ingredient.getName() + " (" + quantity + ")";
    }
}