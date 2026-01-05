package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDao {

    // metoda do zapisywania przepisu w bazie
    public void insertRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipes(title, description) VALUES(?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, recipe.getTitle());
            pstmt.setString(2, recipe.getDescription());
            pstmt.executeUpdate();

            // tutaj do dodanai logika do zapisywania składników (relacja wiele-do-wielu)
            System.out.println("Przepis zapisany w bazie danych: " + recipe.getTitle());

        } catch (SQLException e) {
            System.out.println("Błąd zapisu przepisu: " + e.getMessage());
        }
    }

    // metoda do pobierania wszystkich przepisów
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT id, title, description FROM recipes";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // na razie pusta listę składników, bo ich jeszcze nie pobieramy z bazy
                List<Ingredient> emptyIngredients = new ArrayList<>();

                Recipe recipe = new Recipe(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        emptyIngredients
                );
                recipes.add(recipe);
            }
        } catch (SQLException e) {
            System.out.println("Błąd odczytu: " + e.getMessage());
        }
        return recipes;
    }

    public List<Recipe> searchRecipes(String query) {
        List<Recipe> recipes = new ArrayList<>();
        // lower() sprawia, że wielkość liter nie ma znaczenia (np. "Jajecznica" vs "jajecznica")
        String sql = "SELECT id, title, description FROM recipes WHERE lower(title) LIKE lower(?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // %query% oznacza: szukaj tego tekstu w środku, na początku lub na końcu
            pstmt.setString(1, "%" + query + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tu decydujesz: czy pobierasz składniki od razu, czy pustą listę (Lazy Loading).
                    // Do szybkiej wyszukiwarki pusta lista jest szybsza:
                    recipes.add(new Recipe(
                            rs.getInt("id"), // Pamiętaj o dodaniu pola ID do klasy Recipe!
                            rs.getString("title"),
                            rs.getString("description"),
                            new ArrayList<>() // Pusta lista składników na start
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Błąd wyszukiwania: " + e.getMessage());
        }
        return recipes;
    }

    // zdobywanie nazwy wszystkich składników z bazy danych
    public List<String> getAllIngredientNames() {
        //lista składników
        List<String> names = new ArrayList<>();
        //pobieranie z bazy danych
        String sql = "SELECT name FROM ingredients ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            //pobieranie nazw składników
            while (rs.next()) {
                names.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println("Błąd pobierania nazw składników: " + e.getMessage());
        }
        return names;
    }

    //usuwanie przepisu
    public void deleteRecipe(int recipeId){
        String sqlDeleteIngredients = "DELETE FROM recipe_ingredients WHERE recipe_id = " + recipeId;
        String sqlDeleteRecipe = "DELETE FROM recipes WHERE id = " + recipeId;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            //rozpoczęcie transakcji
            conn.setAutoCommit(false);

            try {
                //usuwamy składniki
                stmt.executeUpdate(sqlDeleteIngredients);
                //usuwamy przepisy
                stmt.executeUpdate(sqlDeleteRecipe);

                conn.commit();
                System.out.println("Usunięto przepis o ID: " + recipeId);

            } catch (SQLException e) {
                //cofanie zmian w razie błędu
                conn.rollback();
                System.out.println("Błąd podczas transakcji: " + e.getMessage());
            } finally {
                //w każdym wypadku wracamy na koniec transakcji
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Błąd podczas usuwania przepisu: " + e.getMessage());
        }
    }
}