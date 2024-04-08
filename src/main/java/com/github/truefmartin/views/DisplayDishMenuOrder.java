package com.github.truefmartin.views;

import com.github.truefmartin.model.DishEntity;
import com.github.truefmartin.model.FoodOrderEntity;
import com.github.truefmartin.model.MenuItemEntity;
import com.github.truefmartin.model.RestaurantEntity;

/*
    Display the dishName, price, date, and time for all orders for that restaurant.
 */
public class DisplayDishMenuOrder {
    public final DishEntity dish;
    public final MenuItemEntity menu;
    public final FoodOrderEntity order;

    public DisplayDishMenuOrder(DishEntity dish, MenuItemEntity menu, FoodOrderEntity order) {
        this.dish = dish;
        this.menu = menu;
        this.order = order;
    }

    @Override
    public String toString() {
        return String.format("DisplayDishMenuOrder{\n" +
                "\tdishName=%s,\n" +
                "\tprice=%.2f\n" +
                "\tdate=%s,\n" +
                "\ttime=%s,\n" +
                '}',
                dish.getDishName(),
                menu.getPrice(),
                order.getDate().toString(),
                order.getTime().toString()
        );
    }
}
