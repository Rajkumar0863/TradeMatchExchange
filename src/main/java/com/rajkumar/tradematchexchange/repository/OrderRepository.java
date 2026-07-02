package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    /**
     * Active orders indexed by Order ID.
     */
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    /**
     * Save a new order.
     */
    public void save(Order order) {

        orders.put(order.getOrderId(), order);
    }

    /**
     * Find order by ID.
     */
    public Optional<Order> findById(String orderId) {

        return Optional.ofNullable(
                orders.get(orderId)
        );
    }

    /**
     * Return all active orders.
     */
    public List<Order> findAll() {

        return new ArrayList<>(
                orders.values()
        );
    }

    /**
     * Check whether an order exists.
     */
    public boolean exists(String orderId) {

        return orders.containsKey(orderId);
    }

    /**
     * Delete an order.
     */
    public void delete(String orderId) {

        orders.remove(orderId);
    }

    /**
     * Update an existing order.
     */
    public void update(Order order) {

        orders.put(
                order.getOrderId(),
                order
        );
    }

    /**
     * Number of active orders.
     */
    public int count() {

        return orders.size();
    }

    /**
     * Remove all orders.
     */
    public void clear() {

        orders.clear();
    }
}