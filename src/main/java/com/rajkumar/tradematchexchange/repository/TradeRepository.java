package com.rajkumar.tradematchexchange.repository;

import com.rajkumar.tradematchexchange.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository
        extends JpaRepository<Trade, String> {
}