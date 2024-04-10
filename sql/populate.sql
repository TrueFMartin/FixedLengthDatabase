INSERT INTO restaurant (restaurant_id, restaurant_name, type, city)
VALUES (1, 'Tasty Thai', 'Asian', 'Dallas'),
       (3, 'Eureka Pizza', 'Pizza', 'Fayetteville'),
       (5, 'Tasty Thai', 'Asian', 'Las Vegas');

INSERT INTO dish (dish_no, dish_name, type)
VALUES (13, 'Spring Rolls', 'ap'),
       (15, 'Pad Thai', 'en'),
       (16, 'Pot Stickers', 'ap'),
       (22, 'Masaman Curry', 'en'),
       (10, 'Custard', 'ds'),
       (12, 'Garlic Bread', 'ap'),
       (44, 'Salad', 'ap'),
       (07, 'Cheese Pizza', 'en'),
       (19, 'Pepperoni Pizza', 'en'),
       (77, 'Veggie Supreme Pizza', 'en');

INSERT INTO menu_item (restaurant_no, dish_no, price)
VALUES (1, 13, 8.00),
       (1, 16, 9.00),
       (1, 44, 10.00),
       (1, 15, 19.00),
       (1, 22, 19.00),
       (3, 44, 6.25),
       (3, 12, 5.50),
       (3, 07, 12.50),
       (3, 19, 13.50),
       (5, 13, 6.00),
       (5, 15, 15.00),
       (5, 22, 14.00);

INSERT INTO food_order (item_no, date, time)
VALUES (3, '2024-03-01', '10:30'),
       (1, '2024-03-02', '15:33'),
       (4, '2024-03-01', '15:35'),
       (6, '2024-03-03', '21:00'),
       (8, '2024-03-01', '18:11'),
       (8, '2024-03-04', '18:51'),
       (10, '2024-03-01', '19:00'),
       (12, '2024-03-05', '17:15'),
       (12, '2024-04-01', '12:10');