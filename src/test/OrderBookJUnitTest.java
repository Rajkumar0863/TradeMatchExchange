package test;

import engine.MatchingEngine;
import engine.OrderBook;
import model.Order;
import model.OrderExecutionType;
import model.OrderType;
import org.junit.jupiter.api.Test;
import repository.TradeRepository;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookJUnitTest {

    @Test
    void shouldExecuteTrade() {

        OrderBook orderBook = new OrderBook();
        MatchingEngine engine = new MatchingEngine();
        TradeRepository repository = new TradeRepository();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                250.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        Order sellOrder = new Order(
                "SELL1",
                "AAPL",
                100,
                200.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        engine.match(orderBook, repository);

        assertEquals(1, repository.getTrades().size());

        assertTrue(orderBook.getBuyOrders().isEmpty());
        assertTrue(orderBook.getSellOrders().isEmpty());
    }

    @Test
    void shouldNotExecuteTradeWhenPricesDoNotMatch() {

        OrderBook orderBook = new OrderBook();
        MatchingEngine engine = new MatchingEngine();
        TradeRepository repository = new TradeRepository();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                150.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        Order sellOrder = new Order(
                "SELL1",
                "AAPL",
                100,
                200.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        engine.match(orderBook, repository);

        assertEquals(0, repository.getTrades().size());

        assertEquals(1, orderBook.getBuyOrders().size());
        assertEquals(1, orderBook.getSellOrders().size());
    }

    @Test
    void shouldHandlePartialFill() {

        OrderBook orderBook = new OrderBook();
        MatchingEngine engine = new MatchingEngine();
        TradeRepository repository = new TradeRepository();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                200,
                250.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        Order sellOrder = new Order(
                "SELL1",
                "AAPL",
                100,
                200.00,
                OrderType.SELL,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        engine.match(orderBook, repository);

        assertEquals(1, repository.getTrades().size());

        assertEquals(100, buyOrder.getQuantity());
        assertEquals(0, sellOrder.getQuantity());

        assertEquals(1, orderBook.getBuyOrders().size());
        assertTrue(orderBook.getSellOrders().isEmpty());
    }

    @Test
    void shouldReturnCorrectBestBidAndBestAsk() {

        OrderBook orderBook = new OrderBook();

        orderBook.addOrder(
                new Order(
                        "BUY1",
                        "AAPL",
                        100,
                        220.00,
                        OrderType.BUY,
                        OrderExecutionType.LIMIT
                )
        );

        orderBook.addOrder(
                new Order(
                        "SELL1",
                        "AAPL",
                        100,
                        225.00,
                        OrderType.SELL,
                        OrderExecutionType.LIMIT
                )
        );

        assertEquals(220.00, orderBook.getBestBid());
        assertEquals(225.00, orderBook.getBestAsk());
    }

    @Test
    void shouldCancelOrderSuccessfully() {

        OrderBook orderBook = new OrderBook();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                210.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);

        assertTrue(orderBook.cancelOrder("BUY1"));

        assertTrue(orderBook.getBuyOrders().isEmpty());
    }

    @Test
    void shouldModifyOrderSuccessfully() {

        OrderBook orderBook = new OrderBook();

        Order buyOrder = new Order(
                "BUY1",
                "AAPL",
                100,
                210.00,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        orderBook.addOrder(buyOrder);

        assertTrue(
                orderBook.modifyOrder(
                        "BUY1",
                        50,
                        215.00
                )
        );

        Order modified = orderBook.getBuyOrders().peek();

        assertNotNull(modified);

        assertEquals(50, modified.getQuantity());
        assertEquals(215.00, modified.getPrice());
    }
}