import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import service.Exchange;

public class Main {

    public static void main(String[] args) {

        System.out.println("==========================================");
        System.out.println("      TradeMatch Exchange v2.0");
        System.out.println("==========================================");

        Exchange exchange = new Exchange();

        /*
         * Create Stocks
         */

        exchange.addStock("AAPL");

        exchange.displayStocks();

        /*
         * BUY Orders
         */

        exchange.placeOrder(

                new Order(
                        "ORD001",
                        "AAPL",
                        100,
                        210.00,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                ));

        exchange.placeOrder(

                new Order(
                        "ORD002",
                        "AAPL",
                        50,
                        215.00,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                ));

        exchange.placeOrder(

                new Order(
                        "ORD003",
                        "AAPL",
                        75,
                        208.00,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                ));

        /*
         * SELL Orders
         */

        exchange.placeOrder(

                new Order(
                        "ORD004",
                        "AAPL",
                        80,
                        205.00,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                ));

        exchange.placeOrder(

                new Order(
                        "ORD005",
                        "AAPL",
                        60,
                        210.00,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                ));

        exchange.placeOrder(

                new Order(
                        "ORD006",
                        "AAPL",
                        40,
                        220.00,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                ));

        /*
         * Before Matching
         */

        System.out.println("\n========== BEFORE MATCHING ==========");

        exchange.displayOrderBook("AAPL");

        /*
         * Execute Matching
         */

        exchange.matchOrders("AAPL");

        /*
         * After Matching
         */

        System.out.println("\n========== AFTER MATCHING ==========");

        exchange.displayOrderBook("AAPL");

        /*
         * Trades
         */

        exchange.displayTradeHistory();

        /*
         * Modify Order
         */

        System.out.println("\n========== MODIFY ORDER ==========");

        exchange.modifyOrder(
                "AAPL",
                "ORD006",
                100,
                218.00
        );

        exchange.displayOrderBook("AAPL");

        /*
         * Cancel Order
         */

        System.out.println("\n========== CANCEL ORDER ==========");

        exchange.cancelOrder(
                "AAPL",
                "ORD003"
        );

        exchange.displayOrderBook("AAPL");

        System.out.println("\n==========================================");
        System.out.println(" Exchange Simulation Completed Successfully");
        System.out.println("==========================================");
    }
}