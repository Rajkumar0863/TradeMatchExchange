package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.exception.OrderNotFoundException;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public OrderResponse placeOrder(OrderRequest request) {

        Order order = new Order(
                request.getOrderId(),
                request.getStockSymbol(),
                request.getQuantity(),
                request.getPrice(),
                OrderType.valueOf(request.getOrderType()),
                OrderExecutionType.valueOf(request.getExecutionType())
        );

        orderRepository.save(order);

        exchange.placeOrder(order);

        exchange.matchOrders(order.getStockSymbol());

        return new OrderResponse(
                "SUCCESS",
                order.getOrderId(),
                "Order placed successfully."
        );
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
    public OrderDto getOrder(String orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found: " + orderId));

        return new OrderDto(order);
    }

    /**
     * Delete an order.
     */
    public OrderResponse deleteOrder(String orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found: " + orderId));

        exchange.cancelOrder(
                order.getStockSymbol(),
                orderId);

        orderRepository.delete(orderId);

        return new OrderResponse(
                "SUCCESS",
                orderId,
                "Order cancelled successfully."
        );
    }

    /**
     * Modify an order.
     */
    public OrderResponse updateOrder(
            String orderId,
            UpdateOrderRequest request) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found: " + orderId));

        boolean updated =
                exchange.modifyOrder(
                        order.getStockSymbol(),
                        orderId,
                        request.getQuantity(),
                        request.getPrice());

        if (!updated) {

            throw new OrderNotFoundException(
                    "Unable to modify order: " + orderId);
        }

        order.setQuantity(request.getQuantity());

        orderRepository.update(order);

        return new OrderResponse(
                "SUCCESS",
                orderId,
                "Order updated successfully."
        );
    }
}