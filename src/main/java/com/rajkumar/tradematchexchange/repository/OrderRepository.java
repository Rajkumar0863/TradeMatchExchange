package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        orders.put(order.getOrderId(), order);
    }

    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public boolean exists(String orderId) {
        return orders.containsKey(orderId);
    }

    public void delete(String orderId) {
        orders.remove(orderId);
    }

    public Collection<Order> findAll() {
        return orders.values();
    }
}