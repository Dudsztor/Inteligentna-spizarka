package org.main.recipeapp.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RecipeIngredientTest {

    @Test
    void testToStringFormat() {
        // dane wejściowe
        Ingredient ing = new Ingredient("Mąka");
        RecipeIngredient recipeIngredient = new RecipeIngredient(ing, "500g");

        // akcja
        String result = recipeIngredient.toString();

        // sprawdzenie
        // powinno być Mąka (ilość)
        assertEquals("Mąka (500g)", result);
    }

    @Test
    void testGetNameReturnsIngredientName() {
        Ingredient ing = new Ingredient("Jajka");
        RecipeIngredient recipeIngredient = new RecipeIngredient(ing, "2 szt");

        assertEquals("Jajka", recipeIngredient.getName());
    }
}