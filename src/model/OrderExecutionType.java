package model;

/**
 * Represents the execution instruction for an order.
 *
 * LIMIT  - Execute only at the specified price or better.
 * MARKET - Execute immediately at the best available price.
 * IOC    - Immediate-Or-Cancel.
 * FOK    - Fill-Or-Kill.
 */
public enum OrderExecutionType {

    LIMIT,

    MARKET,

    IOC,

    FOK
}