package com.github.truefmartin.models;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "restaurant", schema = "fcmartin")
public class RestaurantEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "restaurant_id")
    private int restaurantId;
    @Basic
    @Column(name = "restaurant_name")
    private String restaurantName;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "city")
    private String city;

    @OneToMany(mappedBy = "restaurant")
    private Set<MenuItemEntity> menuItems = new LinkedHashSet<>();

    public Set<MenuItemEntity> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(Set<MenuItemEntity> menuItems) {
        this.menuItems = menuItems;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantEntity that = (RestaurantEntity) o;
        return restaurantId == that.restaurantId && Objects.equals(type, that.type) && Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, restaurantName, type, city);
    }

    @Override
    public String toString() {
        return "RestaurantEntity{" +
                "restaurantId=" + restaurantId +
                ", restaurantName='" + restaurantName + '\'' +
                ", type='" + type + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
