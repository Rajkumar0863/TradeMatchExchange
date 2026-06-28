package service;

import engine.OrderBook;

import java.util.HashMap;
import java.util.Map;

public class Exchange {

    private Map<String, OrderBook> orderBooks;

    public Exchange() {

        orderBooks = new HashMap<>();
    }

    public void addStock(String stockSymbol) {

        if (!orderBooks.containsKey(stockSymbol)) {

            orderBooks.put(
                    stockSymbol,
                    new OrderBook()
            );

            System.out.println(
                    "Created Order Book for: "
                            + stockSymbol
            );
        }
    }

    public OrderBook getOrderBook(
            String stockSymbol) {

        return orderBooks.get(stockSymbol);
    }

    public void displayStocks() {

        System.out.println(
                "\n===== AVAILABLE STOCKS ====="
        );

        for (String stock : orderBooks.keySet()) {

            System.out.println(stock);
        }
    }
}