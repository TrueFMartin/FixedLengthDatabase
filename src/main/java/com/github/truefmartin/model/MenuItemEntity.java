package com.github.truefmartin.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
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
    @Basic
    @Column(name = "restaurant_no")
    private Integer restaurantNo;
//    @Basic
//    @Column(name = "dish_no")
//    private Integer dishNo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dish_no", nullable = true)
    private DishEntity dish;
    
    @Basic
    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "itemNo")
    private Set<FoodOrderEntity> foodOrders = new LinkedHashSet<>();

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

    public Integer getRestaurantNo() {
        return restaurantNo;
    }

    public void setRestaurantNo(Integer restaurantNo) {
        this.restaurantNo = restaurantNo;
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
        return itemNo == that.itemNo && Objects.equals(restaurantNo, that.restaurantNo) && Objects.equals(dish.getDishNo(), that.dish.getDishNo()) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemNo, restaurantNo, dish, price);
    }

    @Override
    public String toString() {
        var dishStr = "null";
        if (this.dish != null) {
            dishStr = String.valueOf(this.dish.getDishNo());
        }
        return "MenuItemEntity{" +
                "itemNo=" + itemNo +
                ", restaurantNo=" + restaurantNo +
                ", dishNo=" + dishStr +
                ", price=" + String.format("%.2f", price) +
                '}';
    }
}

