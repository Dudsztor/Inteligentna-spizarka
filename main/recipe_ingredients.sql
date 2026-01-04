create table recipe_ingredients
(
    recipe_id       INTEGER
        references recipes,
    ingredient_id   INTEGER
        references ingredients,
    quantity_needed TEXT,
    primary key (recipe_id, ingredient_id)
);

