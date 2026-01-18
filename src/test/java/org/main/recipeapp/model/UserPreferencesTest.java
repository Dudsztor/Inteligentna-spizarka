package org.main.recipeapp.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.main.recipeapp.UserPreferences;

import static org.junit.jupiter.api.Assertions.*;

class UserPreferencesTest {

    // wybieramy ID
    private final int TEST_ID = -999;

    @AfterEach
    void cleanUp() {
        // sprzątamy ulubione id
        UserPreferences.removeFavorite(TEST_ID);
    }

    @Test
    void testAddAndCheckFavorite() {
        // krok 1
        assertFalse(UserPreferences.isFavorite(TEST_ID), "Na początku nie powinno być ulubione");

        // krok 2
        UserPreferences.addFavorite(TEST_ID);

        // krok 3
        assertTrue(UserPreferences.isFavorite(TEST_ID), "Powinno być teraz w ulubionych");
    }

    @Test
    void testRemoveFavorite() {
        // krok 1
        UserPreferences.addFavorite(TEST_ID);
        assertTrue(UserPreferences.isFavorite(TEST_ID));

        // krok 2
        UserPreferences.removeFavorite(TEST_ID);

        // krok 3
        assertFalse(UserPreferences.isFavorite(TEST_ID), "Powinno zostać usunięte");
    }
}