package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;
import org.main.recipeapp.model.Ingredient;
import org.main.recipeapp.model.PantryItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PantryDao implements IPantryDao {

    // pobieranie zawartości spiżarni
    public List<PantryItem> getPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        String sql = "SELECT i.id, i.name, p.quantity FROM ingredients i JOIN pantry p ON i.id = p.ingredient_id ORDER BY i.name";

        // połączenie z bazą
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // dopóki są kolejne składniki to dodaje
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Double quantity = rs.getDouble("quantity");

                Ingredient ingredient = new Ingredient(id, name);

                list.add(new PantryItem(ingredient, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // dodawanie do spiżarni. Strict, bo tylko dodajemy to co jest w bazie
    public boolean addIngredientToPantryStrict(String name, Double quantity) {
        int id = getIngredientIdByName(name);

        // jeśli id to -1 to byłby jakiś błąd, bo składniki mają id dodatnie
        if (id == -1) {
            return false;
        }

        //dodaje do spiżarni dane składniki
        String sql = "REPLACE INTO pantry (ingredient_id, quantity, added_date) VALUES (?, ?, CURRENT_DATE)";

        // zmienia wartość ingredient_id i quantity
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // tutaj 1 - pierwsza wartość, czyli ingredient_id
            pstmt.setInt(1, id);
            // tutaj 2 - durga wartość, czyli quantity
            pstmt.setDouble(2, quantity);
            pstmt.executeUpdate();
            //System.out.println("Dodano do spiżarni: " + name);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // usuwanie ze spiżarni
    public void removeFromPantry(int ingredientId) {

        String sql = "DELETE FROM pantry WHERE ingredient_id = ?";

        // połączenie z bazą
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // na miejsce pytajnika daje nazwę składnika
            pstmt.setInt(1, ingredientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // zdobywanie id dzięki nazwę składnika
    public int getIngredientIdByName(String name) {

        String sql = "SELECT id FROM ingredients WHERE lower(name) = lower(?)";

        //połączenie z bazą
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //wyszukuje zamiast pytajnik to nazwę
            pstmt.setString(1, name.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}