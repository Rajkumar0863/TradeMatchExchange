package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
    }

    @Test
    void shouldSaveTrade() {

        Trade trade = createTrade(
                "TRD001",
                "BUY001",
                "SELL001",
                100,
                150.50
        );

        Trade savedTrade =
                tradeRepository.save(trade);

        assertNotNull(savedTrade);
        assertEquals(
                "TRD001",
                savedTrade.getTradeId()
        );
        assertEquals(
                "BUY001",
                savedTrade.getBuyOrderId()
        );
        assertEquals(
                "SELL001",
                savedTrade.getSellOrderId()
        );
        assertEquals(
                100,
                savedTrade.getQuantity()
        );
        assertEquals(
                150.50,
                savedTrade.getExecutionPrice()
        );
        assertNotNull(
                savedTrade.getTimestamp()
        );
    }

    @Test
    void shouldFindTradeById() {

        Trade trade = createTrade(
                "TRD002",
                "BUY002",
                "SELL002",
                200,
                175.25
        );

        tradeRepository.save(trade);

        Optional<Trade> result =
                tradeRepository.findById(
                        "TRD002"
                );

        assertTrue(result.isPresent());

        Trade foundTrade =
                result.get();

        assertEquals(
                "TRD002",
                foundTrade.getTradeId()
        );

        assertEquals(
                200,
                foundTrade.getQuantity()
        );

        assertEquals(
                175.25,
                foundTrade.getExecutionPrice()
        );
    }

    @Test
    void shouldReturnEmptyWhenTradeDoesNotExist() {

        Optional<Trade> result =
                tradeRepository.findById(
                        "UNKNOWN"
                );

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void shouldReturnAllTrades() {

        Trade trade1 = createTrade(
                "TRD003",
                "BUY003",
                "SELL003",
                100,
                200.00
        );

        Trade trade2 = createTrade(
                "TRD004",
                "BUY004",
                "SELL004",
                150,
                205.00
        );

        tradeRepository.save(trade1);
        tradeRepository.save(trade2);

        List<Trade> trades =
                tradeRepository.findAll();

        assertEquals(
                2,
                trades.size()
        );
    }

    @Test
    void shouldCountTrades() {

        tradeRepository.save(
                createTrade(
                        "TRD005",
                        "BUY005",
                        "SELL005",
                        50,
                        300.00
                )
        );

        tradeRepository.save(
                createTrade(
                        "TRD006",
                        "BUY006",
                        "SELL006",
                        75,
                        305.00
                )
        );

        assertEquals(
                2,
                tradeRepository.count()
        );
    }

    @Test
    void shouldDeleteTrade() {

        Trade trade = createTrade(
                "TRD007",
                "BUY007",
                "SELL007",
                120,
                220.00
        );

        tradeRepository.save(trade);

        assertTrue(
                tradeRepository.existsById(
                        "TRD007"
                )
        );

        tradeRepository.deleteById(
                "TRD007"
        );

        assertFalse(
                tradeRepository.existsById(
                        "TRD007"
                )
        );
    }

    @Test
    void shouldDeleteAllTrades() {

        tradeRepository.save(
                createTrade(
                        "TRD008",
                        "BUY008",
                        "SELL008",
                        100,
                        100.00
                )
        );

        tradeRepository.save(
                createTrade(
                        "TRD009",
                        "BUY009",
                        "SELL009",
                        200,
                        110.00
                )
        );

        assertEquals(
                2,
                tradeRepository.count()
        );

        tradeRepository.deleteAll();

        assertEquals(
                0,
                tradeRepository.count()
        );
    }

    private Trade createTrade(
            String tradeId,
            String buyOrderId,
            String sellOrderId,
            int quantity,
            double executionPrice) {

        return new Trade(
                tradeId,
                buyOrderId,
                sellOrderId,
                quantity,
                executionPrice
        );
    }
}