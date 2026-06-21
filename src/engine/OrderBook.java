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
}