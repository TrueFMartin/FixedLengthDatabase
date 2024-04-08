package com.github.truefmartin.views;

import com.github.truefmartin.model.DishEntity;
import com.github.truefmartin.model.MenuItemEntity;
import com.github.truefmartin.model.RestaurantEntity;

/*
If the dish is found, display the itemNo, restaurantName, city and price for all matches.
 */
public class DisplayRestaurantDishMenu {
    public RestaurantEntity restaurant;
    public DishEntity dish;
    public MenuItemEntity menu;

    public DisplayRestaurantDishMenu(RestaurantEntity restaurant, DishEntity dish, MenuItemEntity menu) {
        this.restaurant = restaurant;
        this.dish = dish;
        this.menu = menu;
    }

    @Override
    public String toString() {
        return String.format("DisplayRestaurantDish{\n" +
                "\titemNo=%d,\n" +
                "\trestaurantName=%s,\n" +
                "\tcity=%s,\n" +
                "\tprice=%.2f\n" +
                '}',
                menu.getItemNo(),
                restaurant.getRestaurantName(),
                restaurant.getCity(),
                menu.getPrice());
    }
}
