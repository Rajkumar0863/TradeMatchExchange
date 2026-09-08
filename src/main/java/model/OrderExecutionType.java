package model;

/**
 * Represents the execution instruction
 * associated with an order.
 */
public enum OrderExecutionType {

    /**
     * Execute at the specified price
     * or better.
     */
    LIMIT,

    /**
     * Execute immediately at the
     * best available market price.
     */
    MARKET,

    /**
     * Immediate-Or-Cancel.
     * Execute whatever quantity is
     * immediately available.
     * Cancel the remaining quantity.
     */
    IOC,

    /**
     * Fill-Or-Kill.
     * Execute the complete order
     * immediately.
     * Otherwise cancel the
     * entire order.
     */
    FOK
}