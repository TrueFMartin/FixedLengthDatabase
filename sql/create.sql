CREATE TABLE restaurant
    (
        restaurant_id int
            PRIMARY KEY,
        restaurant_name varchar(15) NOT NULL,
        type varchar(15) NOT NULL,
        city varchar(15) NOT NULL
    );

CREATE TABLE dish
    (
        dish_no int
            PRIMARY KEY,
        dish_name varchar(25) NOT NULL,
        type ENUM ('ap', 'en', 'ds')
    );

CREATE TABLE menu_item
    (
        item_no int
            PRIMARY KEY NOT NULL AUTO_INCREMENT,
        restaurant_no int,
        dish_no int,
        price dec(4, 2)
            CHECK ( price BETWEEN 5 AND 50 ),
        FOREIGN KEY (restaurant_no) REFERENCES restaurant (restaurant_id) ON DELETE NO ACTION,
        FOREIGN KEY (dish_no) REFERENCES dish (dish_no) ON DELETE SET NULL
    );

CREATE TABLE food_order
    (
        order_no int
            PRIMARY KEY NOT NULL AUTO_INCREMENT,
        item_no int,
        date date
            CHECK ( date >= '2024-01-01'),
        time time,
        FOREIGN KEY (item_no) REFERENCES menu_item (item_no) ON DELETE CASCADE
    );