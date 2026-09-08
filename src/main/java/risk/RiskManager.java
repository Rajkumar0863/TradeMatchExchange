package risk;

import model.Order;
import model.OrderExecutionType;

import java.util.HashSet;
import java.util.Set;

public class RiskManager {

    private static final int MAX_ORDER_QUANTITY = 1_000_000;
    private static final double MAX_ORDER_PRICE = 1_000_000.00;

    private final Set<String> orderIds;

    public RiskManager() {

        orderIds = new HashSet<>();
    }

    /**
     * Validates an order before it enters the exchange.
     */
    public boolean validate(Order order) {

        if (order == null) {

            System.out.println("Risk Check Failed : Order is null.");

            return false;
        }

        if (order.getOrderId() == null
                || order.getOrderId().isBlank()) {

            System.out.println("Risk Check Failed : Invalid Order ID.");

            return false;
        }

        if (orderIds.contains(order.getOrderId())) {

            System.out.println("Risk Check Failed : Duplicate Order ID.");

            return false;
        }

        if (order.getStockSymbol() == null
                || order.getStockSymbol().isBlank()) {

            System.out.println("Risk Check Failed : Invalid Stock Symbol.");

            return false;
        }

        if (order.getQuantity() <= 0) {

            System.out.println("Risk Check Failed : Quantity must be greater than zero.");

            return false;
        }

        if (order.getQuantity() > MAX_ORDER_QUANTITY) {

            System.out.println("Risk Check Failed : Quantity exceeds maximum allowed.");

            return false;
        }

        /*
         * LIMIT / IOC / FOK require a valid price.
         * MARKET orders ignore price.
         */

        if (order.getExecutionType() != OrderExecutionType.MARKET) {

            if (order.getPrice() <= 0) {

                System.out.println("Risk Check Failed : Invalid limit price.");

                return false;
            }

            if (order.getPrice() > MAX_ORDER_PRICE) {

                System.out.println("Risk Check Failed : Price exceeds maximum allowed.");

                return false;
            }
        }

        orderIds.add(order.getOrderId());

        return true;
    }

    /**
     * Removes an Order ID from the active registry.
     * Call this when an order is cancelled or fully executed.
     */
    public void removeOrder(String orderId) {

        orderIds.remove(orderId);
    }

    /**
     * Clears all tracked Order IDs.
     */
    public void clear() {

        orderIds.clear();
    }

    /**
     * Checks whether an Order ID already exists.
     */
    public boolean containsOrder(String orderId) {

        return orderIds.contains(orderId);
    }

    /**
     * Returns the number of tracked orders.
     */
    public int getTrackedOrderCount() {

        return orderIds.size();
    }
}