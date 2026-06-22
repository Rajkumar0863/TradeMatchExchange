package engine;

import model.Order;
import model.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class OrderBook {

    private PriorityQueue<Order> buyOrders;
    private PriorityQueue<Order> sellOrders;

    private List<Trade> tradeHistory;
    private int tradeCounter;

    public OrderBook() {

        // BUY SIDE
        // Higher price wins
        // If same price -> earlier timestamp wins

        buyOrders = new PriorityQueue<>(
                (o1, o2) -> {

                    int priceCompare =
                            Double.compare(
                                    o2.getPrice(),
                                    o1.getPrice()
                            );

                    if (priceCompare != 0) {
                        return priceCompare;
                    }

                    return o1.getTimestamp()
                            .compareTo(
                                    o2.getTimestamp()
                            );
                }
        );

        // SELL SIDE
        // Lower price wins
        // If same price -> earlier timestamp wins

        sellOrders = new PriorityQueue<>(
                (o1, o2) -> {

                    int priceCompare =
                            Double.compare(
                                    o1.getPrice(),
                                    o2.getPrice()
                            );

                    if (priceCompare != 0) {
                        return priceCompare;
                    }

                    return o1.getTimestamp()
                            .compareTo(
                                    o2.getTimestamp()
                            );
                }
        );

        tradeHistory = new ArrayList<>();
        tradeCounter = 1;
    }

    public void addBuyOrder(Order order) {

        buyOrders.add(order);

        System.out.println(
                "Added BUY Order: "
                        + order.getOrderId()
        );
    }

    public void addSellOrder(Order order) {

        sellOrders.add(order);

        System.out.println(
                "Added SELL Order: "
                        + order.getOrderId()
        );
    }

    public void displayBuyOrders() {

        PriorityQueue<Order> temp =
                new PriorityQueue<>(buyOrders);

        System.out.println("\nBuy Orders:");

        while (!temp.isEmpty()) {

            System.out.println(temp.poll());
        }
    }

    public void displaySellOrders() {

        PriorityQueue<Order> temp =
                new PriorityQueue<>(sellOrders);

        System.out.println("\nSell Orders:");

        while (!temp.isEmpty()) {

            System.out.println(temp.poll());
        }
    }

    public void matchOrders() {

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            Order bestBuy = buyOrders.peek();
            Order bestSell = sellOrders.peek();

            if (bestBuy.getPrice() >= bestSell.getPrice()) {

                int tradeQuantity =
                        Math.min(
                                bestBuy.getQuantity(),
                                bestSell.getQuantity()
                        );

                Trade trade =
                        new Trade(
                                "TRD" + tradeCounter++,
                                bestBuy.getOrderId(),
                                bestSell.getOrderId(),
                                tradeQuantity,
                                bestSell.getPrice()
                        );

                tradeHistory.add(trade);

                System.out.println(
                        "\nTRADE EXECUTED"
                );

                System.out.println(
                        "BUY : "
                                + bestBuy.getOrderId()
                );

                System.out.println(
                        "SELL: "
                                + bestSell.getOrderId()
                );

                System.out.println(
                        "Trade Quantity: "
                                + tradeQuantity
                );

                System.out.println(
                        "Execution Price: "
                                + bestSell.getPrice()
                );

                bestBuy.setQuantity(
                        bestBuy.getQuantity()
                                - tradeQuantity
                );

                bestSell.setQuantity(
                        bestSell.getQuantity()
                                - tradeQuantity
                );

                if (bestBuy.getQuantity() == 0) {
                    buyOrders.poll();
                }

                if (bestSell.getQuantity() == 0) {
                    sellOrders.poll();
                }

            } else {

                break;
            }
        }
    }

    public void displayTradeHistory() {

        System.out.println(
                "\n===== TRADE HISTORY ====="
        );

        for (Trade trade : tradeHistory) {

            System.out.println(trade);
        }
    }


    public void addOrder(Order order) {

        if (order.getOrderType().name().equals("BUY")) {
            addBuyOrder(order);
        } else {
            addSellOrder(order);
        }
    }

    public void printOrderBook() {

        displayBuyOrders();
        displaySellOrders();
    }

    public List<Trade> getTradeHistory() {
        return tradeHistory;
    }

    public Double getBestBid() {

        if (buyOrders.isEmpty()) {
            return null;
        }

        return buyOrders.peek().getPrice();
    }

    public Double getBestAsk() {

        if (sellOrders.isEmpty()) {
            return null;
        }

        return sellOrders.peek().getPrice();
    }

    public void displayMarketDepth() {

        System.out.println(
                "\n===== MARKET DEPTH ====="
        );

        Double bestBid = getBestBid();
        Double bestAsk = getBestAsk();

        System.out.println(
                "Best Bid: "
                        + (bestBid == null ? "None" : bestBid)
        );

        System.out.println(
                "Best Ask: "
                        + (bestAsk == null ? "None" : bestAsk)
        );

        if (bestBid != null && bestAsk != null) {

            double spread = bestAsk - bestBid;

            System.out.println(
                    "Spread: "
                            + spread
            );

        } else {

            System.out.println(
                    "Spread: N/A"
            );
        }
    }
    public boolean cancelOrder(String orderId) {

        for (Order order : buyOrders) {

            if (order.getOrderId().equals(orderId)) {

                buyOrders.remove(order);

                System.out.println(
                        "Order Cancelled: "
                                + orderId
                );

                return true;
            }
        }

        for (Order order : sellOrders) {

            if (order.getOrderId().equals(orderId)) {

                sellOrders.remove(order);

                System.out.println(
                        "Order Cancelled: "
                                + orderId
                );

                return true;
            }
        }

        System.out.println(
                "Order Not Found: "
                        + orderId
        );

        return false;
    }
    public boolean modifyOrder(
            String orderId,
            int newQuantity,
            double newPrice) {

        for (Order order : buyOrders) {

            if (order.getOrderId().equals(orderId)) {

                buyOrders.remove(order);

                Order modifiedOrder =
                        new Order(
                                order.getOrderId(),
                                order.getStockSymbol(),
                                newQuantity,
                                newPrice,
                                order.getOrderType(),
                                order.getExecutionType()
                        );

                buyOrders.add(modifiedOrder);

                System.out.println(
                        "Order Modified: "
                                + orderId
                );

                return true;
            }
        }

        for (Order order : sellOrders) {

            if (order.getOrderId().equals(orderId)) {

                sellOrders.remove(order);

                Order modifiedOrder =
                        new Order(
                                order.getOrderId(),
                                order.getStockSymbol(),
                                newQuantity,
                                newPrice,
                                order.getOrderType(),
                                order.getExecutionType()
                        );

                sellOrders.add(modifiedOrder);

                System.out.println(
                        "Order Modified: "
                                + orderId
                );

                return true;
            }
        }

        System.out.println(
                "Order Not Found: "
                        + orderId
        );

        return false;
    }
}