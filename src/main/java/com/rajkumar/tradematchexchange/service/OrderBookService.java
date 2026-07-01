package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderBookResponse;
import com.rajkumar.tradematchexchange.engine.OrderBook;
import com.rajkumar.tradematchexchange.model.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class OrderBookService {

    private final Exchange exchange;

    public OrderBookService(Exchange exchange) {
        this.exchange = exchange;
    }

    public OrderBookResponse getOrderBook(String stockSymbol) {

        OrderBook orderBook = exchange.getOrderBook(stockSymbol);

        if (orderBook == null) {
            throw new IllegalArgumentException(
                    "No Order Book found for stock: " + stockSymbol
            );
        }

        List<Order> buyOrders = new ArrayList<>();

        PriorityQueue<Order> buyCopy =
                new PriorityQueue<>(orderBook.getBuyOrders());

        while (!buyCopy.isEmpty()) {
            buyOrders.add(buyCopy.poll());
        }

        List<Order> sellOrders = new ArrayList<>();

        PriorityQueue<Order> sellCopy =
                new PriorityQueue<>(orderBook.getSellOrders());

        while (!sellCopy.isEmpty()) {
            sellOrders.add(sellCopy.poll());
        }

        return new OrderBookResponse(
                stockSymbol,
                orderBook.getBestBid(),
                orderBook.getBestAsk(),
                buyOrders,
                sellOrders
        );
    }
}