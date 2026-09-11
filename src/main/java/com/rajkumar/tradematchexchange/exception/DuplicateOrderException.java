package com.rajkumar.tradematchexchange.exception;

public class DuplicateOrderException extends RuntimeException {

    public DuplicateOrderException(String orderId) {
        super("An active order already exists with ID: " + orderId);
    }
}