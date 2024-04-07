package com.github.truefmartin.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "dish", schema = "fcmartin")
public class DishEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "dish_no")
    private int dishNo;
    @Basic
    @Column(name = "dish_name")
    private String dishName;
    @Basic
    @Enumerated(EnumType.STRING)
    private Type type;

    public int getDishNo() {
        return dishNo;
    }

    public void setDishNo(int dishNo) {
        this.dishNo = dishNo;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DishEntity that = (DishEntity) o;
        return dishNo == that.dishNo && Objects.equals(dishName, that.dishName) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishNo, dishName, type);
    }

    @Override
    public String toString() {
        return "DishEntity{" +
                "dishNo=" + dishNo +
                ", dishName='" + dishName + '\'' +
                ", type=" + type.toString() +
                '}';
    }
}