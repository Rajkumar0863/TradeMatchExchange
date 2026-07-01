package com.rajkumar.tradematchexchange.dto;

public class OrderResponse {

    private String status;
    private String orderId;
    private String message;

    public OrderResponse() {
    }

    public OrderResponse(String status, String orderId, String message) {
        this.status = status;
        this.orderId = orderId;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}