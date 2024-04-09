package com.github.truefmartin.views;

import com.github.truefmartin.models.DishEntity;
import com.github.truefmartin.models.RestaurantEntity;

public class BuildDishPrequery {
    RestaurantEntity restaurant;
    DishEntity lastDish;

    public BuildDishPrequery(RestaurantEntity restaurant, DishEntity dish) {
        this.restaurant = restaurant;
        this.lastDish = dish;
    }

    public int getLastDishNo() {
        return lastDish.getDishNo();
    }

    public boolean doesRestaurantExist() {
        return restaurant != null;
    }

    public int getRestaurantId() {
        if (restaurant == null) {
            return -1;
        }
        return restaurant.getRestaurantId();
    }
}
