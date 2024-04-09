package com.github.truefmartin.views;

import com.github.truefmartin.models.DishEntity;
import com.github.truefmartin.models.FoodOrderEntity;
import com.github.truefmartin.models.RestaurantEntity;

/*
    Display all food orders (orderNo, dishName, restaurantName, date, time).
 */
public class DisplayRestaurantDishOrder {
    RestaurantEntity restaurant;
    DishEntity dish;
    FoodOrderEntity order;

    public DisplayRestaurantDishOrder(RestaurantEntity restaurant, DishEntity dish, FoodOrderEntity order) {
        this.restaurant = restaurant;
        this.dish = dish;
        this.order = order;
    }

    @Override
    public String toString() {
        return String.format("DisplayRestaurantDishOrder{\n" +
                        "\torderNo=%d,\n" +
                        "\tdishName=%s,\n" +
                        "\trestaurantName=%s,\n" +
                        "\tdate=%s\n" +
                        "\ttime=%s\n" +
                        '}',
                order.getOrderNo(),
                dish.getDishName(),
                restaurant.getRestaurantName(),
                order.getDate().toString(),
                order.getTime().toString()
        );
    }

    public FoodOrderEntity getOrder() {
        return order;
    }
}
