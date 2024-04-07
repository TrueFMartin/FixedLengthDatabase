package com.github.truefmartin.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

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
    @Basic
    @Column(name = "dish_no")
    private Integer dishNo;
    @Basic
    @Column(name = "price")
    private BigDecimal price;

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

    public Integer getDishNo() {
        return dishNo;
    }

    public void setDishNo(Integer dishNo) {
        this.dishNo = dishNo;
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
        return itemNo == that.itemNo && Objects.equals(restaurantNo, that.restaurantNo) && Objects.equals(dishNo, that.dishNo) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemNo, restaurantNo, dishNo, price);
    }

    @Override
    public String toString() {
        return "MenuItemEntity{" +
                "itemNo=" + itemNo +
                ", restaurantNo=" + restaurantNo +
                ", dishNo=" + dishNo +
                ", price=" + price +
                '}';
    }
}

