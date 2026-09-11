package com.rajkumar.tradematchexchange.service;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.repository.OrderRepository;

@Component
public class OrderBookRecoveryService
        implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final Exchange exchange;

    public OrderBookRecoveryService(
            OrderRepository orderRepository,
            Exchange exchange) {

        this.orderRepository =
                orderRepository;

        this.exchange =
                exchange;
    }

    /**
     * Rebuilds the in-memory order books from
     * active orders stored in PostgreSQL.
     *
     * Runs automatically once Spring Boot
     * has finished creating the application context.
     */
    @Override
    public void run(
            ApplicationArguments args) {

        List<Order> persistedOrders =
                orderRepository.findAll();

        System.out.println();

        System.out.println(
                "===================================="
        );

        System.out.println(
                "      ORDER BOOK RECOVERY"
        );

        System.out.println(
                "===================================="
        );

        if (persistedOrders.isEmpty()) {

            System.out.println(
                    "No active orders to restore."
            );

            System.out.println(
                    "===================================="
            );

            return;
        }

        int restoredOrders = 0;

        for (Order order
                : persistedOrders) {

            exchange.restoreOrder(
                    order
            );

            restoredOrders++;
        }

        System.out.println(
                "Recovered Orders : "
                        + restoredOrders
        );

        System.out.println(
                "===================================="
        );
    }
}