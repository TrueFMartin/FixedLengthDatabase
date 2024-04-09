package com.github.truefmartin.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "menu_item", schema = "fcmartin")
public class MenuItemEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "item_no")
    private int itemNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restaurant_no", nullable = false)
    private RestaurantEntity restaurant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dish_no", nullable = true)
    private DishEntity dish;
    
    @Basic
    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "menu", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<FoodOrderEntity> foodOrders = new HashSet<>();

    public Set<FoodOrderEntity> getFoodOrders() {
        return foodOrders;
    }

    public void setFoodOrders(Set<FoodOrderEntity> foodOrders) {
        this.foodOrders = foodOrders;
    }

    public int getItemNo() {
        return itemNo;
    }

    public void setItemNo(int itemNo) {
        this.itemNo = itemNo;
    }

    public RestaurantEntity getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantEntity restaurant) {
        this.restaurant = restaurant;
    }

    public DishEntity getDish() {
        return dish;
    }

    public void setDish(DishEntity dishNo) {
        this.dish = dishNo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemEntity that = (MenuItemEntity) o;
        return itemNo == that.itemNo && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemNo, price);
    }

    @Override
    public String toString() {
        var dishStr = "null";
        if (this.dish != null) {
            dishStr = String.valueOf(this.dish.getDishNo());
        }

        var restaurantStr = "null";
        if (this.restaurant != null) {
            restaurantStr = String.valueOf(this.restaurant.getRestaurantId());
        }

        return "MenuItemEntity{" +
                "itemNo=" + itemNo +
                ", restaurantNo=" + restaurantStr +
                ", dishNo=" + dishStr +
                ", price=" + String.format("%.2f", price) +
                '}';
    }
}

