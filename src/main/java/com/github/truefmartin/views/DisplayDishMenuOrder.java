package com.github.truefmartin.views;

import com.github.truefmartin.models.DishEntity;
import com.github.truefmartin.models.FoodOrderEntity;
import com.github.truefmartin.models.MenuItemEntity;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/*
    Display the dishName, price, date, and time for all orders for that restaurant.
 */
public class DisplayDishMenuOrder {
    public final DishEntity dish;
    public final MenuItemEntity menu;
    public final FoodOrderEntity order;
    private final String alternativeText;

    public DisplayDishMenuOrder(DishEntity dish, MenuItemEntity menu, FoodOrderEntity order) {
        this.dish = dish;
        this.menu = menu;
        this.order = order;
        alternativeText = null;
    }
    public DisplayDishMenuOrder(Object[] untypedObjects) {
        dish = null;
        menu = null;
        order = null;

        String dName = (String) untypedObjects[0];
        BigDecimal price = (BigDecimal) untypedObjects[1];
        Date date = (Date) untypedObjects[2];
        Time time = (Time) untypedObjects[3];
        this.alternativeText = String.format("DisplayDishMenuOrder{\n" +
                "\t%s\n" +
                "\t%.2f\n" +
                "\t%s\n" +
                "\t%s\n" +
                "}", dName, price.floatValue(), date.toString(), time.toString());
    }

    @Override
    public String toString() {
        if (alternativeText != null) {
            return alternativeText;
        }
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
