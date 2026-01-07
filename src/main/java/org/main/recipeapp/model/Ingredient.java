package org.main.recipeapp.model;

public class Ingredient {
    private int id;
    private String name;

    public Ingredient(String name) { this.name = name; }
    public Ingredient(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() {return id;}
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}
