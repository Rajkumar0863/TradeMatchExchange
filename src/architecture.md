# Architecture

## System Flow

```text
BUY ORDERS (Max Heap)
        |
        v
   Matching Engine
        |
        v
SELL ORDERS (Min Heap)

        |
        v

   Trade History
```

## Components

- Order
- OrderBook
- Trade
- MarketStatistics