package repository;

import model.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeRepository {

    private final List<Trade> trades;

    public TradeRepository() {

        this.trades = new ArrayList<>();
    }

    public void addTrade(Trade trade) {

        trades.add(trade);
    }

    public List<Trade> getTrades() {

        return Collections.unmodifiableList(trades);
    }

    public int getTradeCount() {

        return trades.size();
    }

    public boolean isEmpty() {

        return trades.isEmpty();
    }

    public void clear() {

        trades.clear();
    }
}