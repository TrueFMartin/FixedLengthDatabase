package com.github.truefmartin.model;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Time;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "food_order", schema = "fcmartin")
public class FoodOrderEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "order_no")
    private int orderNo;
    @Basic
    @Column(name = "item_no")
    private Integer itemNo;
    @Basic
    @Column(name = "date")
    private Date date;
    @Basic
    @Column(name = "time")
    private Time time;

    /**
     * Loads the Food order entity with the date and time set to
     * the current date/time in UTC. orderNo is generated automatically.
     *
     */
    public void setDateTimeNow() {
        Instant now = Instant.now();
        java.util.Date utilDate = Date.from(now);
        this.date = new java.sql.Date(utilDate.getTime());
        this.time = new java.sql.Time(utilDate.getTime());
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getItemNo() {
        return itemNo;
    }

    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodOrderEntity that = (FoodOrderEntity) o;
        return orderNo == that.orderNo && Objects.equals(itemNo, that.itemNo) && Objects.equals(date, that.date) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, itemNo, date, time);
    }

    @Override
    public String toString() {
        return "FoodOrderEntity{" +
                "orderNo=" + orderNo +
                ", itemNo=" + itemNo +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
