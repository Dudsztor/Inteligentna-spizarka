package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.PantryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListDao implements IShoppingListDao {

    // pobieranie listy zakupów
    public List<PantryItem> getShoppingList() {
        List<PantryItem> list = new ArrayList<>();
        String sql = "SELECT i.id, i.name, s.quantity FROM ingredients i JOIN shopping_list s ON i.id = s.ingredient_id ORDER BY i.name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new PantryItem(
                        new Ingredient(rs.getInt("id"), rs.getString("name")),
                        rs.getDouble("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // dodawanie listy zakupów
    public boolean addToShoppingList(String name, Double quantity) {
        // szukanie id
        IngredientDao tempDao = new IngredientDao();
        int id = tempDao.getIngredientIdByName(name);

        if (id == -1) return false;

        String sql = "REPLACE INTO shopping_list (ingredient_id, quantity) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setDouble(2, quantity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // usuwanie z listy zakupów
    public void removeFromShoppingList(int ingredientId) {
        String sql = "DELETE FROM shopping_list WHERE ingredient_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ingredientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // przenoszenie składnika do spiżarni
    public void buyItem(int ingredientId, double quantity) {
        // dodawanie do spiżarni
        String sqlAddToPantry = """
            INSERT INTO pantry (ingredient_id, quantity, added_date) 
            VALUES (?, ?, CURRENT_DATE)
            ON CONFLICT(ingredient_id) 
            DO UPDATE SET quantity = quantity + excluded.quantity
        """;

        // usuwanie z listy zakupów
        String sqlDeleteFromList = "DELETE FROM shopping_list WHERE ingredient_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // transakcja
            conn.setAutoCommit(false);

            try (PreparedStatement pantryStmt = conn.prepareStatement(sqlAddToPantry);
                 PreparedStatement listStmt = conn.prepareStatement(sqlDeleteFromList)) {

                // dodawanie do spiżarni
                pantryStmt.setInt(1, ingredientId);
                pantryStmt.setDouble(2, quantity);
                pantryStmt.executeUpdate();

                // usuwanie z zakupów
                listStmt.setInt(1, ingredientId);
                listStmt.executeUpdate();


                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}