package com.github.truefmartin.views;

import com.github.truefmartin.models.MenuItemEntity;
import com.github.truefmartin.models.RestaurantEntity;

/*
If the dish is found, display the itemNo, restaurantName, city and price for all matches.
 */
public class DisplayRestaurantMenu {
    public RestaurantEntity restaurant;
    public MenuItemEntity menu;

    public DisplayRestaurantMenu(RestaurantEntity restaurant, MenuItemEntity menu) {
        this.restaurant = restaurant;
        this.menu = menu;
    }

    @Override
    public String toString() {
        return String.format("DisplayRestaurantMenu{\n" +
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

    public int getItemNo() {
        if (menu == null) {
            return -1;
        }
        return menu.getItemNo();
    }

    public MenuItemEntity getMenu() {
        return menu;
    }
}
