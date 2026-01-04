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
}