package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.exception.OrderNotFoundException;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final Exchange exchange;
    private final OrderRepository orderRepository;

    public OrderService(
            Exchange exchange,
            OrderRepository orderRepository) {

        this.exchange = exchange;
        this.orderRepository = orderRepository;

        exchange.addStock("AAPL");
    }

    /**
     * Place a new order.
     */
    @Transactional
    public OrderResponse placeOrder(
            OrderRequest request) {

        Order order =
                new Order(
                        request.getOrderId(),
                        request.getStockSymbol(),
                        request.getQuantity(),
                        request.getPrice(),
                        OrderType.valueOf(
                                request.getOrderType()
                        ),
                        OrderExecutionType.valueOf(
                                request.getExecutionType()
                        )
                );

        orderRepository.save(order);

        exchange.placeOrder(order);

        List<Order> affectedOrders =
                exchange.matchOrders(
                        order.getStockSymbol()
                );

        synchronizeAffectedOrders(
                order.getStockSymbol(),
                affectedOrders
        );

        return new OrderResponse(
                "SUCCESS",
                order.getOrderId(),
                "Order placed successfully."
        );
    }

    /**
     * Synchronise only orders changed during matching.
     */
    private void synchronizeAffectedOrders(
            String stockSymbol,
            List<Order> affectedOrders) {

        if (affectedOrders == null
                || affectedOrders.isEmpty()) {

            return;
        }

        OrderBook orderBook =
                exchange.getOrderBook(
                        stockSymbol
                );

        if (orderBook == null) {

            throw new IllegalStateException(
                    "Order Book not found for stock: "
                            + stockSymbol
            );
        }

        Set<String> activeOrderIds =
                new HashSet<>();

        orderBook.getBuyOrders()
                .forEach(order ->
                        activeOrderIds.add(
                                order.getOrderId()
                        )
                );

        orderBook.getSellOrders()
                .forEach(order ->
                        activeOrderIds.add(
                                order.getOrderId()
                        )
                );

        for (Order affectedOrder
                : affectedOrders) {

            if (activeOrderIds.contains(
                    affectedOrder.getOrderId())) {

                orderRepository.save(
                        affectedOrder
                );

            } else {

                orderRepository.deleteById(
                        affectedOrder.getOrderId()
                );
            }
        }
    }

    /**
     * Return all active orders.
     */
    public List<OrderDto> getAllOrders() {

        return orderRepository
                .findAll()
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Find order by ID.
     */
    public OrderDto getOrder(
            String orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        return new OrderDto(order);
    }

    /**
     * Delete an order.
     */
    @Transactional
    public OrderResponse deleteOrder(
            String orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        exchange.cancelOrder(
                order.getStockSymbol(),
                orderId
        );

        orderRepository.deleteById(
                orderId
        );

        return new OrderResponse(
                "SUCCESS",
                orderId,
                "Order cancelled successfully."
        );
    }

    /**
     * Modify an order.
     */
    @Transactional
    public OrderResponse updateOrder(
            String orderId,
            UpdateOrderRequest request) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        boolean updated =
                exchange.modifyOrder(
                        order.getStockSymbol(),
                        orderId,
                        request.getQuantity(),
                        request.getPrice()
                );

        if (!updated) {

            throw new OrderNotFoundException(
                    "Unable to modify order: "
                            + orderId
            );
        }

        order.setQuantity(
                request.getQuantity()
        );

        order.setPrice(
                request.getPrice()
        );

        orderRepository.save(order);

        return new OrderResponse(
                "SUCCESS",
                orderId,
                "Order updated successfully."
        );
    }
}