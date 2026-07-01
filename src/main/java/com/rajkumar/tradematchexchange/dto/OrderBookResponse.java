package com.rajkumar.tradematchexchange.dto;

import com.rajkumar.tradematchexchange.model.Order;

import java.util.List;

public class OrderBookResponse {

    private String stockSymbol;

    private Double bestBid;

    private Double bestAsk;

    private List<Order> buyOrders;

    private List<Order> sellOrders;

    public OrderBookResponse() {
    }

    public OrderBookResponse(
            String stockSymbol,
            Double bestBid,
            Double bestAsk,
            List<Order> buyOrders,
            List<Order> sellOrders) {

        this.stockSymbol = stockSymbol;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.buyOrders = buyOrders;
        this.sellOrders = sellOrders;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Double getBestBid() {
        return bestBid;
    }

    public void setBestBid(Double bestBid) {
        this.bestBid = bestBid;
    }

    public Double getBestAsk() {
        return bestAsk;
    }

    public void setBestAsk(Double bestAsk) {
        this.bestAsk = bestAsk;
    }

    public List<Order> getBuyOrders() {
        return buyOrders;
    }

    public void setBuyOrders(List<Order> buyOrders) {
        this.buyOrders = buyOrders;
    }

    public List<Order> getSellOrders() {
        return sellOrders;
    }

    public void setSellOrders(List<Order> sellOrders) {
        this.sellOrders = sellOrders;
    }
}