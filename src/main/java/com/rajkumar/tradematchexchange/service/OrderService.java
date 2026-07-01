package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Exchange exchange;

    public OrderService(Exchange exchange) {

        this.exchange = exchange;

        exchange.addStock("AAPL");
    }

    public OrderResponse placeOrder(OrderRequest request) {

        Order order = new Order(
                request.getOrderId(),
                request.getStockSymbol(),
                request.getQuantity(),
                request.getPrice(),
                OrderType.valueOf(request.getOrderType()),
                OrderExecutionType.valueOf(request.getExecutionType())
        );

        exchange.placeOrder(order);

        exchange.matchOrders(request.getStockSymbol());

        return new OrderResponse(
                "SUCCESS",
                request.getOrderId(),
                "Order placed successfully!"
        );
    }
}