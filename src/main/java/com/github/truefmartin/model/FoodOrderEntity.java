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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_no", nullable = false)
    private MenuItemEntity menu;

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

    public MenuItemEntity getMenu() {
        return menu;
    }

    public void setMenu(MenuItemEntity menu) {
        this.menu = menu;
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
        return orderNo == that.orderNo && date == that.date && time == that.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, menu, date, time);
    }

    @Override
    public String toString() {
        var itemNoStr = "null";
        if (this.menu != null) {
            itemNoStr = String.valueOf(this.menu.getItemNo());
        }
        return "FoodOrderEntity{" +
                "orderNo=" + orderNo +
                ", itemNo=" + itemNoStr +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
