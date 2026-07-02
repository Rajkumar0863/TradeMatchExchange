package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.exception.OrderNotFoundException;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private Exchange exchange;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderRequest request;
    private UpdateOrderRequest updateRequest;

    @BeforeEach
    void setUp() {

        order = new Order(
                "ORD001",
                "AAPL",
                100,
                200.0,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );

        request = new OrderRequest();

        request.setOrderId("ORD001");
        request.setStockSymbol("AAPL");
        request.setQuantity(100);
        request.setPrice(200.0);
        request.setOrderType("BUY");
        request.setExecutionType("LIMIT");

        updateRequest = new UpdateOrderRequest();

        updateRequest.setQuantity(150);
        updateRequest.setPrice(220.0);
    }

    @Nested
    @DisplayName("Place Order Tests")
    class PlaceOrderTests {

        @Test
        @DisplayName("Should place order successfully")
        void shouldPlaceOrderSuccessfully() {

            OrderResponse response =
                    orderService.placeOrder(request);

            verify(orderRepository)
                    .save(any(Order.class));

            verify(exchange)
                    .placeOrder(any(Order.class));

            verify(exchange)
                    .matchOrders("AAPL");

            assertEquals(
                    "SUCCESS",
                    response.getStatus());

            assertEquals(
                    "ORD001",
                    response.getOrderId());
        }
    }
        @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order by id")
        void shouldReturnOrderById() {

            when(orderRepository.findById("ORD001"))
                    .thenReturn(Optional.of(order));

            var result = orderService.getOrder("ORD001");

            assertNotNull(result);
            assertEquals("ORD001", result.getOrderId());
            assertEquals("AAPL", result.getStockSymbol());
            assertEquals(100, result.getQuantity());

            verify(orderRepository)
                    .findById("ORD001");
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {

            when(orderRepository.findById("ORD999"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.getOrder("ORD999")
            );

            verify(orderRepository)
                    .findById("ORD999");
        }
    }

    @Nested
    @DisplayName("Get All Orders Tests")
    class GetAllOrdersTests {

        @Test
        @DisplayName("Should return all active orders")
        void shouldReturnAllOrders() {

            Order secondOrder =
                    new Order(
                            "ORD002",
                            "AAPL",
                            50,
                            190,
                            OrderType.SELL,
                            OrderExecutionType.LIMIT
                    );

            when(orderRepository.findAll())
                    .thenReturn(
                            List.of(order, secondOrder)
                    );

            var orders =
                    orderService.getAllOrders();

            assertEquals(2, orders.size());

            verify(orderRepository)
                    .findAll();
        }

        @Test
        @DisplayName("Should return empty list")
        void shouldReturnEmptyList() {

            when(orderRepository.findAll())
                    .thenReturn(List.of());

            var orders =
                    orderService.getAllOrders();

            assertTrue(orders.isEmpty());

            verify(orderRepository)
                    .findAll();
        }
    }

    @Nested
    @DisplayName("Delete Order Tests")
    class DeleteOrderTests {

        @Test
        @DisplayName("Should delete order successfully")
        void shouldDeleteOrderSuccessfully() {

            when(orderRepository.findById("ORD001"))
                    .thenReturn(Optional.of(order));

            when(exchange.cancelOrder(
                    "AAPL",
                    "ORD001"))
                    .thenReturn(true);

            OrderResponse response =
                    orderService.deleteOrder("ORD001");

            verify(exchange)
                    .cancelOrder(
                            "AAPL",
                            "ORD001");

            verify(orderRepository)
                    .delete("ORD001");

            assertEquals(
                    "SUCCESS",
                    response.getStatus());
        }

        @Test
        @DisplayName("Should throw exception while deleting missing order")
        void shouldThrowExceptionForMissingDelete() {

            when(orderRepository.findById("ORD001"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.deleteOrder("ORD001")
            );
        }
    }
        @Nested
    @DisplayName("Update Order Tests")
    class UpdateOrderTests {

        @Test
        @DisplayName("Should update order successfully")
        void shouldUpdateOrderSuccessfully() {

            when(orderRepository.findById("ORD001"))
                    .thenReturn(Optional.of(order));

            when(exchange.modifyOrder(
                    "AAPL",
                    "ORD001",
                    150,
                    220.0))
                    .thenReturn(true);

            OrderResponse response =
                    orderService.updateOrder(
                            "ORD001",
                            updateRequest);

            verify(exchange, times(1))
                    .modifyOrder(
                            "AAPL",
                            "ORD001",
                            150,
                            220.0);

            verify(orderRepository, times(1))
                    .update(any(Order.class));

            assertEquals(
                    "SUCCESS",
                    response.getStatus());

            assertEquals(
                    "ORD001",
                    response.getOrderId());

            assertEquals(
                    "Order updated successfully.",
                    response.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating missing order")
        void shouldThrowExceptionWhenOrderDoesNotExist() {

            when(orderRepository.findById("ORD999"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.updateOrder(
                            "ORD999",
                            updateRequest)
            );

            verify(orderRepository)
                    .findById("ORD999");

            verify(exchange, never())
                    .modifyOrder(
                            anyString(),
                            anyString(),
                            anyInt(),
                            anyDouble());
        }

        @Test
        @DisplayName("Should throw exception when exchange update fails")
        void shouldThrowExceptionWhenExchangeFails() {

            when(orderRepository.findById("ORD001"))
                    .thenReturn(Optional.of(order));

            when(exchange.modifyOrder(
                    "AAPL",
                    "ORD001",
                    150,
                    220.0))
                    .thenReturn(false);

            assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.updateOrder(
                            "ORD001",
                            updateRequest)
            );

            verify(exchange)
                    .modifyOrder(
                            "AAPL",
                            "ORD001",
                            150,
                            220.0);

            verify(orderRepository, never())
                    .update(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Verification Tests")
    class VerificationTests {

        @Test
        @DisplayName("Repository save should be called exactly once")
        void shouldCallSaveOnce() {

            orderService.placeOrder(request);

            verify(orderRepository, times(1))
                    .save(any(Order.class));
        }

        @Test
        @DisplayName("Matching engine should be triggered once")
        void shouldCallMatchingEngineOnce() {

            orderService.placeOrder(request);

            verify(exchange, times(1))
                    .matchOrders("AAPL");
        }

        @Test
        @DisplayName("Exchange should receive placed order")
        void shouldCallExchangePlaceOrder() {

            orderService.placeOrder(request);

            verify(exchange, times(1))
                    .placeOrder(any(Order.class));
        }
    }
}