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

        /*
         * AAPL is currently the default instrument
         * supported by the REST order workflow.
         */
        exchange.addStock("AAPL");
    }

    /**
     * Places a new order.
     *
     * Flow:
     *
     * 1. Convert request DTO to domain Order.
     * 2. Persist the active order.
     * 3. Submit it to the in-memory exchange.
     * 4. Run matching.
     * 5. Synchronise affected orders with PostgreSQL.
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

        /*
         * Persist the order before matching.
         *
         * If the order is fully executed,
         * synchronisation below removes it again.
         */
        orderRepository.save(order);

        /*
         * Add order to the in-memory order book.
         *
         * Exchange performs risk validation before
         * accepting it into the book.
         */
        exchange.placeOrder(order);

        /*
         * Run matching for this instrument.
         *
         * MatchingEngine returns only orders whose
         * state changed during this matching cycle.
         */
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
     * Synchronises only orders affected by matching.
     *
     * If an affected order is still present in the
     * in-memory book, it remains active and is saved
     * with its remaining quantity.
     *
     * If it is no longer present, it has either been:
     *
     * - completely filled
     * - cancelled by IOC
     * - rejected/cancelled by FOK
     * - exhausted as a MARKET order
     *
     * and must therefore be removed from active orders
     * in the database.
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

                /*
                 * Order remains active,
                 * usually after a partial fill.
                 */
                orderRepository.save(
                        affectedOrder
                );

            } else {

                /*
                 * Order is no longer active.
                 */
                orderRepository.deleteById(
                        affectedOrder.getOrderId()
                );
            }
        }
    }

    /**
     * Returns all currently active orders.
     */
    public List<OrderDto> getAllOrders() {

        return orderRepository
                .findAll()
                .stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns one active order by ID.
     */
    public OrderDto getOrder(
            String orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        orderId
                                )
                        );

        return new OrderDto(order);
    }

    /**
     * Cancels and deletes an active order.
     */
    @Transactional
    public OrderResponse deleteOrder(
            String orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        orderId
                                )
                        );

        /*
         * Remove from the in-memory order book first.
         */
        exchange.cancelOrder(
                order.getStockSymbol(),
                orderId
        );

        /*
         * Remove from persistent active orders.
         */
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
     * Modifies quantity and price of an active order.
     *
     * Current behaviour:
     * Exchange updates the order in the in-memory
     * order book and the corresponding persisted
     * order is then updated.
     *
     * We will separately verify timestamp /
     * price-time priority consistency for modification.
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
                                        orderId
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
                    orderId
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