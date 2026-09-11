package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.exception.DuplicateOrderException;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateOrderServiceTest {

    @Mock
    private Exchange exchange;

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {

        orderService =
                new OrderService(
                        exchange,
                        orderRepository
                );
    }

    @Test
    @DisplayName(
            "Duplicate active order ID is rejected before state is modified"
    )
    void shouldRejectDuplicateOrderBeforeStateModification() {

        OrderRequest request =
                new OrderRequest();

        request.setOrderId("ORD001");
        request.setStockSymbol("AAPL");
        request.setQuantity(100);
        request.setPrice(200.0);
        request.setOrderType("BUY");
        request.setExecutionType("LIMIT");

        when(orderRepository.existsById("ORD001"))
                .thenReturn(true);

        DuplicateOrderException exception =
                assertThrows(
                        DuplicateOrderException.class,
                        () -> orderService.placeOrder(
                                request
                        )
                );

        assertEquals(
                "An active order already exists with ID: ORD001",
                exception.getMessage()
        );

        verify(orderRepository)
                .existsById("ORD001");

        verify(
                orderRepository,
                never()
        ).save(
                any(Order.class)
        );

        verify(
                exchange,
                never()
        ).placeOrder(
                any(Order.class)
        );

        verify(
                exchange,
                never()
        ).matchOrders(
                any()
        );
    }
}