package org.main.recipeapp.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortTest {

    @Test
    void testFavouritesSorting() {
        // tworzymy 3 przepisy
        Recipe r1 = new Recipe("A_Zwykły", "", new ArrayList<>());
        r1.setFavorite(false);

        Recipe r2 = new Recipe("B_Ulubiony", "", new ArrayList<>());
        r2.setFavorite(true);

        Recipe r3 = new Recipe("C_Zwykły", "", new ArrayList<>());
        r3.setFavorite(false);

        List<Recipe> recipes = new ArrayList<>();
        recipes.add(r1);
        recipes.add(r2);
        recipes.add(r3);

        // logika sortowania
        recipes.sort((rec1, rec2) -> {
            if (rec1.isFavorite() && !rec2.isFavorite()) return -1;
            if (!rec1.isFavorite() && rec2.isFavorite()) return 1;
            return rec1.getTitle().compareToIgnoreCase(rec2.getTitle());
        });

        // sprawdzamy czy faktycznie sie ustawiły po kolei
        assertEquals("B_Ulubiony", recipes.get(0).getTitle());
        assertEquals("A_Zwykły", recipes.get(1).getTitle());
        assertEquals("C_Zwykły", recipes.get(2).getTitle());
    }
}