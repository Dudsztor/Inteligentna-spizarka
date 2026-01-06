package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.Recipe;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDao {

    // metoda do zapisywania przepisu w bazie
    public void insertRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipes(title, description) VALUES(?, ?)";

        // elementy w try to zasoby, które zamkną się po wykonaniu bloku
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, recipe.getTitle());
            pstmt.setString(2, recipe.getDescription());
            pstmt.executeUpdate();

            // bierzemy ID nowego przepisu
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newRecipeId = generatedKeys.getInt(1);
                    insertRecipeIngredients(newRecipeId, recipe.getIngredients(), conn);
                }
            }
            System.out.println("Przepis zapisany w bazie danych: " + recipe.getTitle());
        } catch (SQLException e) {
            System.out.println("Błąd zapisu przepisu: " + e.getMessage());
        }
    }

    private void insertRecipeIngredients(int recipeId, List<Ingredient> ingredients, Connection conn) throws SQLException {
        String sql = "INSERT INTO recipe_ingredients(recipe_id, ingredient) " +
                "VALUES(?, (SELECT id FROM ingredients WHERE lower(name) = lower(?)))";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Ingredient ing : ingredients) {
                pstmt.setInt(1, recipeId);
                pstmt.setString(2, ing.getName());
                pstmt.addBatch(); //dodajemy do kolejki
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println("Błąd zapisu składników przepisu: " + e.getMessage());
        }
    }

    public List<Recipe> getDoableRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        //podwojna negacja 😰

        String sql = "SELECT r.id, r.title, r.description " +
                     "FROM recipes r " +
                     "WHERE NOT EXISTS (" +
                     "  SELECT 1 FROM recipe_ingredients ri " +
                     "  WHERE ri.recipe_id = r.id " +
                     "  AND ri.ingredient_id NOT IN (SELECT ingredient_id from pantry)" +
                     ")";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                //trzeba tu bylo dodac conn w argumentach zeby nie zrywalo polaczenia
                List<Ingredient> ingredients = getIngredientsForRecipeId(id, conn);

                if (ingredients.isEmpty()) continue;

                recipes.add(new Recipe(id, rs.getString("title"), rs.getString("description"), ingredients));
            }
        } catch (SQLException e) {
            System.out.println("Błąd smart wyszukiwania: " + e.getMessage());
        }
        return recipes;
    }

    private List<Ingredient> getIngredientsForRecipeId(int recipeId, Connection conn) {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT i.name FROM ingredients i " +
                "JOIN recipe_ingredients ri ON i.id = ri.ingredient_id " +
                "WHERE ri.recipe_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    list.add(new Ingredient(rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // metoda do pobierania wszystkich przepisów
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT id, title, description FROM recipes";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                List<Ingredient> emptyIngredients = getIngredientsForRecipeId(id, conn);

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
                    int id = rs.getInt("id");
                    List<Ingredient> ingredients = getIngredientsForRecipeId(id, conn);
                    recipes.add(new Recipe(
                            id,
                            rs.getString("title"),
                            rs.getString("description"),
                            ingredients
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