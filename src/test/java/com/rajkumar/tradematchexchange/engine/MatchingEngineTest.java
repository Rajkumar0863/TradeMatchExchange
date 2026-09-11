package com.rajkumar.tradematchexchange.engine;

import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.model.Trade;
import com.rajkumar.tradematchexchange.repository.TradeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchingEngineTest {

    private MatchingEngine matchingEngine;
    private OrderBook orderBook;
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {

        matchingEngine =
                new MatchingEngine();

        orderBook =
                new OrderBook();

        tradeRepository =
                mock(TradeRepository.class);
    }

    @Test
    void shouldFullyMatchCrossingLimitOrders() {

        Order buy =
                createOrder(
                        "BUY001",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        Order sell =
                createOrder(
                        "SELL001",
                        100,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(buy);
        orderBook.addOrder(sell);

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        assertEquals(
                0,
                buy.getQuantity()
        );

        assertEquals(
                0,
                sell.getQuantity()
        );

        assertEquals(
                2,
                affected.size()
        );

        verify(
                tradeRepository,
                times(1)
        ).save(any(Trade.class));
    }

    @Test
    void shouldNotMatchNonCrossingLimitOrders() {

        Order buy =
                createOrder(
                        "BUY002",
                        100,
                        190.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        Order sell =
                createOrder(
                        "SELL002",
                        100,
                        200.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(buy);
        orderBook.addOrder(sell);

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        assertEquals(
                1,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                1,
                orderBook.getSellOrderCount()
        );

        assertEquals(
                100,
                buy.getQuantity()
        );

        assertEquals(
                100,
                sell.getQuantity()
        );

        assertTrue(
                affected.isEmpty()
        );

        verify(
                tradeRepository,
                never()
        ).save(any(Trade.class));
    }

    @Test
    void shouldHandlePartialFill() {

        Order buy =
                createOrder(
                        "BUY003",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        Order sell =
                createOrder(
                        "SELL003",
                        40,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(buy);
        orderBook.addOrder(sell);

        matchingEngine.match(
                orderBook,
                tradeRepository
        );

        assertEquals(
                60,
                buy.getQuantity()
        );

        assertEquals(
                0,
                sell.getQuantity()
        );

        assertEquals(
                1,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        verify(
                tradeRepository,
                times(1)
        ).save(any(Trade.class));
    }

    @Test
    void shouldUseBestPricePriority() {

        Order expensiveSell =
                createOrder(
                        "SELL-HIGH",
                        50,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order cheaperSell =
                createOrder(
                        "SELL-LOW",
                        50,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order buy =
                createOrder(
                        "BUY004",
                        50,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(expensiveSell);
        orderBook.addOrder(cheaperSell);
        orderBook.addOrder(buy);

        matchingEngine.match(
                orderBook,
                tradeRepository
        );

        ArgumentCaptor<Trade> captor =
                ArgumentCaptor.forClass(
                        Trade.class
                );

        verify(
                tradeRepository
        ).save(captor.capture());

        Trade trade =
                captor.getValue();

        assertEquals(
                "SELL-LOW",
                trade.getSellOrderId()
        );

        assertEquals(
                190.0,
                trade.getExecutionPrice()
        );

        assertEquals(
                1,
                orderBook.getSellOrderCount()
        );
    }

    @Test
    void shouldUseTimePriorityWhenPricesAreEqual()
            throws InterruptedException {

        Order firstSell =
                createOrder(
                        "SELL-FIRST",
                        50,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        /*
         * Ensure timestamps are observably different.
         */
        Thread.sleep(5);

        Order secondSell =
                createOrder(
                        "SELL-SECOND",
                        50,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order buy =
                createOrder(
                        "BUY005",
                        50,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(secondSell);
        orderBook.addOrder(firstSell);
        orderBook.addOrder(buy);

        matchingEngine.match(
                orderBook,
                tradeRepository
        );

        ArgumentCaptor<Trade> captor =
                ArgumentCaptor.forClass(
                        Trade.class
                );

        verify(
                tradeRepository
        ).save(captor.capture());

        assertEquals(
                "SELL-FIRST",
                captor.getValue()
                        .getSellOrderId()
        );
    }

    @Test
    void shouldExecuteIocAgainstAvailableLiquidityAndCancelRemainder() {

        Order sellOne =
                createOrder(
                        "SELL-IOC-1",
                        30,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order sellTwo =
                createOrder(
                        "SELL-IOC-2",
                        30,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order iocBuy =
                createOrder(
                        "BUY-IOC",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.IOC
                );

        orderBook.addOrder(sellOne);
        orderBook.addOrder(sellTwo);
        orderBook.addOrder(iocBuy);

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        /*
         * 60 units execute and remaining
         * 40 units are cancelled.
         */
        assertEquals(
                40,
                iocBuy.getQuantity()
        );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        assertTrue(
                affected.stream()
                        .anyMatch(order ->
                                order.getOrderId()
                                        .equals("BUY-IOC"))
        );

        verify(
                tradeRepository,
                times(2)
        ).save(any(Trade.class));
    }

    @Test
    void shouldRemoveIocWhenNothingCanExecute() {

        Order iocBuy =
                createOrder(
                        "BUY-IOC-NO-MATCH",
                        100,
                        190.0,
                        OrderType.BUY,
                        OrderExecutionType.IOC
                );

        Order sell =
                createOrder(
                        "SELL-LIMIT",
                        100,
                        200.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        orderBook.addOrder(iocBuy);
        orderBook.addOrder(sell);

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                1,
                orderBook.getSellOrderCount()
        );

        assertTrue(
                affected.stream()
                        .anyMatch(order ->
                                order.getOrderId()
                                        .equals("BUY-IOC-NO-MATCH"))
        );

        verify(
                tradeRepository,
                never()
        ).save(any(Trade.class));
    }

    @Test
    void shouldFullyExecuteFokAcrossMultipleCounterparties() {

        Order sellOne =
                createOrder(
                        "SELL-FOK-1",
                        40,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order sellTwo =
                createOrder(
                        "SELL-FOK-2",
                        60,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order fokBuy =
                createOrder(
                        "BUY-FOK",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.FOK
                );

        orderBook.addOrder(sellOne);
        orderBook.addOrder(sellTwo);
        orderBook.addOrder(fokBuy);

        matchingEngine.match(
                orderBook,
                tradeRepository
        );

        assertEquals(
                0,
                fokBuy.getQuantity()
        );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        verify(
                tradeRepository,
                times(2)
        ).save(any(Trade.class));
    }

    @Test
    void shouldRejectFokWhenExecutableLiquidityIsInsufficient() {

        /*
         * Total SELL volume = 150.
         *
         * But only 50 can execute at BUY price 200.
         * SELL-TOO-EXPENSIVE cannot participate.
         */
        Order executableSell =
                createOrder(
                        "SELL-EXECUTABLE",
                        50,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order expensiveSell =
                createOrder(
                        "SELL-TOO-EXPENSIVE",
                        100,
                        250.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order fokBuy =
                createOrder(
                        "BUY-FOK-REJECT",
                        100,
                        200.0,
                        OrderType.BUY,
                        OrderExecutionType.FOK
                );

        orderBook.addOrder(executableSell);
        orderBook.addOrder(expensiveSell);
        orderBook.addOrder(fokBuy);

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        /*
         * FOK must execute completely or not at all.
         */
        assertEquals(
                100,
                fokBuy.getQuantity()
        );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                2,
                orderBook.getSellOrderCount()
        );

        assertTrue(
                affected.stream()
                        .anyMatch(order ->
                                order.getOrderId()
                                        .equals("BUY-FOK-REJECT"))
        );

        verify(
                tradeRepository,
                never()
        ).save(any(Trade.class));
    }

    @Test
    void shouldExecuteMarketOrderAcrossAvailableLiquidity() {

        Order sellOne =
                createOrder(
                        "SELL-MKT-1",
                        30,
                        190.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order sellTwo =
                createOrder(
                        "SELL-MKT-2",
                        40,
                        195.0,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                );

        Order marketBuy =
                createOrder(
                        "BUY-MARKET",
                        70,
                        0.0,
                        OrderType.BUY,
                        OrderExecutionType.MARKET
                );

        orderBook.addOrder(sellOne);
        orderBook.addOrder(sellTwo);
        orderBook.addOrder(marketBuy);

        matchingEngine.match(
                orderBook,
                tradeRepository
        );

        assertEquals(
                0,
                marketBuy.getQuantity()
        );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        verify(
                tradeRepository,
                times(2)
        ).save(any(Trade.class));
    }

    @Test
    void shouldRemoveMarketOrderWhenNoLiquidityExists() {

        Order marketBuy =
                createOrder(
                        "BUY-MARKET-EMPTY",
                        100,
                        0.0,
                        OrderType.BUY,
                        OrderExecutionType.MARKET
                );

        orderBook.addOrder(
                marketBuy
        );

        List<Order> affected =
                matchingEngine.match(
                        orderBook,
                        tradeRepository
                );

        assertEquals(
                0,
                orderBook.getBuyOrderCount()
        );

        assertEquals(
                0,
                orderBook.getSellOrderCount()
        );

        assertTrue(
                affected.stream()
                        .anyMatch(order ->
                                order.getOrderId()
                                        .equals("BUY-MARKET-EMPTY"))
        );

        verify(
                tradeRepository,
                never()
        ).save(any(Trade.class));
    }

    /**
     * Helper for creating AAPL orders.
     */
    private Order createOrder(
            String orderId,
            int quantity,
            double price,
            OrderType orderType,
            OrderExecutionType executionType) {

        return new Order(
                orderId,
                "AAPL",
                quantity,
                price,
                orderType,
                executionType
        );
    }
}