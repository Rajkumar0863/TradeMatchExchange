package com.rajkumar.tradematchexchange.controller;

import com.rajkumar.tradematchexchange.dto.OrderBookResponse;
import com.rajkumar.tradematchexchange.service.OrderBookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderbook")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping("/{symbol}")
    public OrderBookResponse getOrderBook(
            @PathVariable String symbol) {

        return orderBookService.getOrderBook(symbol);
    }
}