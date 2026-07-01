package test;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.service.Exchange;

public class OrderBookTest {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("      ORDER BOOK MANUAL TEST");
        System.out.println("====================================");

        Exchange exchange = new Exchange();

        exchange.addStock("AAPL");

        exchange.placeOrder(
                new Order(
                        "BUY1",
                        "AAPL",
                        100,
                        250.00,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        exchange.placeOrder(
                new Order(
                        "SELL1",
                        "AAPL",
                        100,
                        200.00,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        System.out.println("\n========== BEFORE MATCHING ==========");
        exchange.displayOrderBook("AAPL");

        exchange.matchOrders("AAPL");

        System.out.println("\n========== AFTER MATCHING ==========");
        exchange.displayOrderBook("AAPL");

        System.out.println("\n========== TRADE HISTORY ==========");
        exchange.displayTradeHistory();

        System.out.println("\n====================================");
        System.out.println("        TEST COMPLETED");
        System.out.println("====================================");
    }
}