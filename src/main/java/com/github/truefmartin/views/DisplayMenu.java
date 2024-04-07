package com.github.truefmartin.views;

import com.github.truefmartin.model.DishEntity;
import com.github.truefmartin.model.MenuItemEntity;
import com.github.truefmartin.model.RestaurantEntity;
/*
    Find and list all menu items available from that restaurant location.
    Output the restaurant name once (echo the user input) and then list the dish name and price for each
    available menu item.
 */
public class DisplayMenu {
    RestaurantEntity restaurant;
    MenuItemEntity menu;
    DishEntity dish;

    public DisplayMenu(RestaurantEntity restaurant, MenuItemEntity menu, DishEntity dish) {
        this.restaurant = restaurant;
        this.menu = menu;
        this.dish = dish;
    }

    @Override
    public String toString() {
        return String.format("DisplayMenu{\n" +
                "\tdishName=%s,\n" +
                "\tprice=%.2f\n" +
                '}', dish.getDishName(),  menu.getPrice());
    }
}
