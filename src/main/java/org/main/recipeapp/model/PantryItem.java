package org.main.recipeapp.model;

public class PantryItem {
    private String name;
    private String quantity;

    // konstruktor
    public PantryItem(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getQuantity() { return quantity; }

    // formatowanie do wierszy, np. wpisując Ananas i 2 to zmieni na Ananas (2)
    @Override
    public String toString() {
        if (quantity == null || quantity.isEmpty()) {
            return name;
        }
        return name + " (" + quantity + ")";
    }
}