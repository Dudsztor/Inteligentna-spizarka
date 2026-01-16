package org.main.recipeapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    //LORAK
    //private static final String url = "jdbc:sqlite:spizarka/Inteligentna-spizarka/spizarka.db";

    //DUDSZTOR
    private static final String url = "jdbc:sqlite:spizarka.db";

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Publiczna metoda dostępu
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}