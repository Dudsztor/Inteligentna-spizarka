module org.example.recipeapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.recipeapp to javafx.fxml;
    exports org.example.recipeapp;
}