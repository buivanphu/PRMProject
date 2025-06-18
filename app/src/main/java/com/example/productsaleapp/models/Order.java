package com.example.productsaleapp.models;

public class Order {
    public int id;
    public double totalAmount;
    public String status;
    public String orderDate;

    public Order(int id, String status, double total, String date) {
        this.id = id;
        this.status = status;
        this.totalAmount = total;
        this.orderDate = date;
    }


}
