module org.example.recipeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.recipeapp to javafx.fxml;
    exports org.example.recipeapp;
}