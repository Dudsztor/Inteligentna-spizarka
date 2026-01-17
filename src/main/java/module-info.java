module org.main.recipeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires junit;
    requires java.prefs;
//    requires org.main.recipeapp;


    opens org.main.recipeapp to javafx.fxml;
    exports org.main.recipeapp;
    exports org.main.recipeapp.controllers;
    opens org.main.recipeapp.controllers to javafx.fxml;
}