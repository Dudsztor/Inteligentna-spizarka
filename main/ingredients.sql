create table ingredients
(
    id   INTEGER
        primary key autoincrement,
    name TEXT not null
        unique
);

