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

### Order
Represents a buy or sell order.

### OrderBook
Maintains:
- Buy Orders (Max Heap)
- Sell Orders (Min Heap)

### Trade
Represents an executed trade.

### Matching Engine
Matches:
- Highest Buy Order
- Lowest Sell Order

using Price-Time Priority.

## Matching Logic

1. Fetch best buy order
2. Fetch best sell order
3. Check if buy price >= sell price
4. Execute trade
5. Update quantities
6. Record trade history
