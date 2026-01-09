package org.main.recipeapp.dao;

import org.main.recipeapp.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IngredientDao implements IIngredientDao{
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
