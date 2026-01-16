package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.MissingItemRecipe;
import org.main.recipeapp.model.Recipe;
import org.main.recipeapp.model.RecipeIngredient;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDao implements IRecipeDao {

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
                    insertRecipeIngredientsInternal(newRecipeId, recipe.getIngredients(), conn);
                }
            }
            System.out.println("Przepis zapisany w bazie danych: " + recipe.getTitle());
        } catch (SQLException e) {
            System.out.println("Błąd zapisu przepisu: " + e.getMessage());
        }
    }

    @Override
    public void insertRecipeIngredients(int recipeId, List<RecipeIngredient> ingredients) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // wywołujemy metodę ale pokazując jej połączenie
            insertRecipeIngredientsInternal(recipeId, ingredients, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertRecipeIngredientsInternal(int recipeId, List<RecipeIngredient> ingredients, Connection conn) throws SQLException {
        String sql = "INSERT INTO recipe_ingredients(recipe_id, ingredient_id, quantity_needed) " +
                "VALUES(?, (SELECT id FROM ingredients WHERE lower(name) = lower(?)), ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (RecipeIngredient item : ingredients) { // Iterujemy po RecipeIngredient
                pstmt.setInt(1, recipeId);
                pstmt.setString(2, item.getName());     // Pobieramy nazwę z wnętrza
                pstmt.setDouble(3, item.getQuantity()); // Pobieramy ilość
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public List<Recipe> getDoableRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        //podwojna negacja 😰

        String sql = """
            SELECT r.id, r.title, r.description
            FROM recipes r
            WHERE NOT EXISTS (
                SELECT 1
                FROM recipe_ingredients ri
                LEFT JOIN pantry p ON ri.ingredient_id = p.ingredient_id
                WHERE ri.recipe_id = r.id
                AND (
                    p.ingredient_id IS NULL 
                    OR 
                    p.quantity < ri.quantity_needed
                )
            )
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");

                List<RecipeIngredient> ingredients = getIngredientsForRecipeIdInternal(id, conn);

                if (ingredients.isEmpty()) continue;

                recipes.add(new Recipe(id, rs.getString("title"), rs.getString("description"), ingredients));
            }
        } catch (SQLException e) {
            System.out.println("Błąd smart wyszukiwania: " + e.getMessage());
        }
        return recipes;
    }

    @Override
    public List<RecipeIngredient> getIngredientsForRecipeId(int recipeId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return getIngredientsForRecipeIdInternal(recipeId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<RecipeIngredient> getIngredientsForRecipeIdInternal(int recipeId, Connection conn) {
        List<RecipeIngredient> list = new ArrayList<>();
        String sql = "SELECT i.id, i.name, ri.quantity_needed FROM ingredients i " +
                "JOIN recipe_ingredients ri ON i.id = ri.ingredient_id " +
                "WHERE ri.recipe_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    Ingredient ing = new Ingredient(id, name);

                    // skladnik
                    double quantity = rs.getDouble("quantity_needed");

                    // połaczenie
                    list.add(new RecipeIngredient(ing, quantity));
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
                List<RecipeIngredient> ingredients = getIngredientsForRecipeIdInternal(id, conn);

                Recipe recipe = new Recipe(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        ingredients
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
                    List<RecipeIngredient> ingredients = getIngredientsForRecipeIdInternal(id, conn);
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

    // pobieranie listy składników i ich ilości do przepisu
    public List<String> getIngredientsForRecipe(int recipeId) {
        List<String> result = new ArrayList<>();

        // wybieranie nazwy i potrzebnej ilości z bazy
        String sql = """
            SELECT i.name, ri.quantity_needed 
            FROM recipe_ingredients ri
            JOIN ingredients i ON ri.ingredient_id = i.id
            WHERE ri.recipe_id = ?
        """;

        // połączenie z bazą
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // w miejsce pytajnika daje recipeId
            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();

            // szuka po bazie
            while (rs.next()) {
                String name = rs.getString("name");
                Double qty = rs.getDouble("quantity_needed");
                // formatowanie, jak jest ilość to ją pokazuje, jak nie ma to nie ma nic
                if (qty == 0.0) {
                    result.add("- " + name);
                } else {
                    result.add("- " + name + " - " + qty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<MissingItemRecipe> getAlmostDoableRecipes(int limit) {
        List<MissingItemRecipe> result = new ArrayList<>();

        // łączymy przepis ze składnikami i spiżarnią
        // filtrujemy tylko te wiersze gdzie brakuje skladnika albo jest za malo
        //grupujemy po id przepisu
        // bierzemy tam gdzie jest dokladnie 1 brakujacy
        // zwracane jest co i ile

        String sql = """
            SELECT r.id, r.title, r.description,
                   i.id AS ing_id, i.name AS ing_name,
                   (ri.quantity_needed - IFNULL(p.quantity, 0)) AS missing_amount
            FROM recipes r
            JOIN recipe_ingredients ri ON r.id = ri.recipe_id
            JOIN ingredients i ON ri.ingredient_id = i.id
            LEFT JOIN pantry p ON ri.ingredient_id = p.ingredient_id
            WHERE (p.quantity IS NULL OR p.quantity < ri.quantity_needed)
            GROUP BY r.id
            HAVING COUNT(*) = 1
            LIMIT ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try(ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recipeId = rs.getInt("id");
                    String title = rs.getString("title");
                    String desc = rs.getString("description");

                    List<RecipeIngredient> fullIngredients = getIngredientsForRecipeIdInternal(recipeId, conn);
                    Recipe recipe = new Recipe(recipeId, title, desc, fullIngredients);

                    int missingIngId = rs.getInt("ing_id");
                    String missingIngName = rs.getString("ing_name");
                    double missingAmount = rs.getDouble("missing_amount");

                    Ingredient missingIngredient = new Ingredient(missingIngId, missingIngName);

                    result.add(new MissingItemRecipe(recipe, missingIngredient, missingAmount));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}