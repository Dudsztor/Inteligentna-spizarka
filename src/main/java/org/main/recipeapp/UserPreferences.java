package org.main.recipeapp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

public class UserPreferences {

    // klucz, pod którym system pamięta ulubione
    private static final String FAVORITES_KEY = "user_favorite_recipes_ids";

    // ustawienia aplikacji
    private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);

    // sprawdzanie czy ID jest ulubione
    public static boolean isFavorite(int recipeId) {
        Set<String> ids = getFavoritesSet();
        return ids.contains(String.valueOf(recipeId));
    }

    // dodawanie do ulubionych
    public static void addFavorite(int recipeId) {
        Set<String> ids = getFavoritesSet();
        ids.add(String.valueOf(recipeId));
        saveFavoritesSet(ids);
    }

    // usuwanie z ulubionych
    public static void removeFavorite(int recipeId) {
        Set<String> ids = getFavoritesSet();
        ids.remove(String.valueOf(recipeId));
        saveFavoritesSet(ids);
    }

    // pobieranie napisu i zmiana na zbiór
    private static Set<String> getFavoritesSet() {
        String savedString = prefs.get(FAVORITES_KEY, "");

        if (savedString.isEmpty()) {
            return new HashSet<>();
        }

        // dzielenie przecinkiem
        return new HashSet<>(Arrays.asList(savedString.split(",")));
    }

    // zmiana zbioru na napis
    private static void saveFavoritesSet(Set<String> ids) {
        String joined = String.join(",", ids);
        prefs.put(FAVORITES_KEY, joined);
    }
}