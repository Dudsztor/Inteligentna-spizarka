create table pantry
(
    ingredient_id INTEGER
        primary key
        references ingredients,
    quantity      TEXT,
    added_date    DATE
);

