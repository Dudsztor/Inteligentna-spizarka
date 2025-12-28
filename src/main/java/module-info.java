module org.main.recipeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;


    opens org.main.recipeapp to javafx.fxml;
    exports org.main.recipeapp;
}