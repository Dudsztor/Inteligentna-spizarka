package org.main.recipeapp.dao;

import org.main.recipeapp.model.PantryItem;

import java.util.List;

public interface IShoppingListDao {
    List<PantryItem> getShoppingList();
    boolean addToShoppingList(String name, Double quantity);
    void removeFromShoppingList(int ingredientId);
    void buyItem(int ingredientId, double quantity);
}
