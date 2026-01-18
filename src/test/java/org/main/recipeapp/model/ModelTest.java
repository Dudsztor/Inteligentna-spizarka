package org.main.recipeapp.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testMissingItemRecipeRounding() {
        // dane wejściowe
        // tworzenie przepisu i składnika
        Recipe dummyRecipe = new Recipe("Test", "Opis", new ArrayList<>());
        Ingredient dummyIngredient = new Ingredient("Mąka");

        // symulujemy że brakuje
        double rawMissingQuantity = 1.234567;

        // brakujący item
        MissingItemRecipe missingItem = new MissingItemRecipe(dummyRecipe, dummyIngredient, rawMissingQuantity);

        // sprawdzamy czy zaokrągla
        assertEquals(1.23, missingItem.getMissingQuantity(), 0.001, "Zaokrąglenie");
    }

    @Test
    void testRecipeIngredientLogic() {
        // dane
        Ingredient ing = new Ingredient("Cukier");
        RecipeIngredient ri = new RecipeIngredient(ing, 5.0);

        // dane z bay
        ri.setQuantityInPantry(2.0);
        ri.setQuantityInShoppingList(6.0);

        // sprawdzamy czy jest równe
        assertEquals("Cukier", ri.getName());
        assertEquals(5.0, ri.getQuantity());
        assertEquals(2.0, ri.getQuantityInPantry());
        assertEquals(6.0, ri.getQuantityInShoppingList());
    }
}