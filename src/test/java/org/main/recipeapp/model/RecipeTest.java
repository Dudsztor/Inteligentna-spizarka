package org.main.recipeapp.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void testRecipeCreation() {
        // tworzenie obiektu, bo recipeingredient potrzebuje obiektu
        Ingredient jajko = new Ingredient(1, "Jajko");

        // tworzenie recipeingredient
        RecipeIngredient skladnikPrzepisu = new RecipeIngredient(jajko, "2 szt");

        // tworzymy liste
        List<RecipeIngredient> ingredientsList = new ArrayList<>();
        ingredientsList.add(skladnikPrzepisu);

        // tworzenie przepisu
        Recipe recipe = new Recipe(999, "Jajecznica (dobra)", "Usmaż jajka na maśle", ingredientsList);

        // sprawdzanie danych przepisu
        assertEquals(999, recipe.getId());
        assertEquals("Jajecznica (dobra)", recipe.getTitle());

        // sprawdzamy czy lista nie jest pusta
        assertNotNull(recipe.getIngredients());
        assertEquals(1, recipe.getIngredients().size());

        // sprawdzamy dane wewnątrz listy
        RecipeIngredient pobranySkladnik = recipe.getIngredients().get(0);

        // test metod z RecipeIngredient
        assertEquals("2 szt", pobranySkladnik.getQuantity());
        assertEquals("Jajko", pobranySkladnik.getName());

        // testujemy czy w środku jest dobry obiekt Ingredient
        assertEquals(1, pobranySkladnik.getIngredient().getId());
    }
}