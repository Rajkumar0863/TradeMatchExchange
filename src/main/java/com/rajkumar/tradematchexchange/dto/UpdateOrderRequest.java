package com.rajkumar.tradematchexchange.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class UpdateOrderRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Positive(message = "Price must be greater than 0")
    private double price;

    public UpdateOrderRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}