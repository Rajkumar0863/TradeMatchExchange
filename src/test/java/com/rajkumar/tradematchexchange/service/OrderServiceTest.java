package com.rajkumar.tradematchexchange.service;

import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.exception.OrderNotFoundException;
import com.rajkumar.tradematchexchange.model.Order;
import com.rajkumar.tradematchexchange.model.OrderExecutionType;
import com.rajkumar.tradematchexchange.model.OrderType;
import com.rajkumar.tradematchexchange.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private Exchange exchange;

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {

        orderService = new OrderService(
                exchange,
                orderRepository
        );
    }

    private Order createOrder(
            String orderId,
            int quantity,
            double price) {

        return new Order(
                orderId,
                "AAPL",
                quantity,
                price,
                OrderType.BUY,
                OrderExecutionType.LIMIT
        );
    }

    @Nested
    class PlaceOrderTests {

        @Test
        void shouldPlaceOrderSuccessfully() {

            OrderRequest request =
                    mock(OrderRequest.class);

            when(request.getOrderId())
                    .thenReturn("ORD001");

            when(request.getStockSymbol())
                    .thenReturn("AAPL");

            when(request.getQuantity())
                    .thenReturn(100);

            when(request.getPrice())
                    .thenReturn(150.0);

            when(request.getOrderType())
                    .thenReturn("BUY");

            when(request.getExecutionType())
                    .thenReturn("LIMIT");

            OrderResponse response =
                    orderService.placeOrder(request);

            assertNotNull(response);

            verify(orderRepository)
                    .save(any(Order.class));

            verify(exchange)
                    .placeOrder(any(Order.class));

            verify(exchange)
                    .matchOrders("AAPL");
        }
    }

    @Nested
    class GetOrderTests {

        @Test
        void shouldReturnOrderWhenOrderExists() {

            Order order =
                    createOrder(
                            "ORD002",
                            50,
                            200.0
                    );

            when(orderRepository.findById("ORD002"))
                    .thenReturn(Optional.of(order));

            OrderDto result =
                    orderService.getOrder("ORD002");

            assertNotNull(result);

            verify(orderRepository)
                    .findById("ORD002");
        }

        @Test
        void shouldThrowExceptionWhenOrderDoesNotExist() {

            when(orderRepository.findById("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.getOrder("UNKNOWN")
            );

            verify(orderRepository)
                    .findById("UNKNOWN");
        }
    }

    @Nested
    class GetAllOrdersTests {

        @Test
        void shouldReturnAllOrders() {

            Order first =
                    createOrder(
                            "ORD003",
                            100,
                            150.0
                    );

            Order second =
                    createOrder(
                            "ORD004",
                            200,
                            160.0
                    );

            when(orderRepository.findAll())
                    .thenReturn(
                            List.of(first, second)
                    );

            List<OrderDto> result =
                    orderService.getAllOrders();

            assertNotNull(result);
            assertEquals(2, result.size());

            verify(orderRepository)
                    .findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoOrdersExist() {

            when(orderRepository.findAll())
                    .thenReturn(List.of());

            List<OrderDto> result =
                    orderService.getAllOrders();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(orderRepository)
                    .findAll();
        }
    }

    @Nested
    class DeleteOrderTests {

        @Test
        void shouldDeleteExistingOrder() {

            Order order =
                    createOrder(
                            "ORD005",
                            100,
                            175.0
                    );

            when(orderRepository.findById("ORD005"))
                    .thenReturn(Optional.of(order));

            OrderResponse response =
                    orderService.deleteOrder("ORD005");

            assertNotNull(response);

            verify(exchange)
                    .cancelOrder(
                            "AAPL",
                            "ORD005"
                    );

            verify(orderRepository)
                    .deleteById("ORD005");
        }

        @Test
        void shouldThrowExceptionWhenDeletingUnknownOrder() {

            when(orderRepository.findById("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () ->
                            orderService.deleteOrder(
                                    "UNKNOWN"
                            )
            );

            verify(orderRepository)
                    .findById("UNKNOWN");

            verify(orderRepository, never())
                    .deleteById(anyString());

            verify(exchange, never())
                    .cancelOrder(
                            anyString(),
                            anyString()
                    );
        }
    }

    @Nested
    class UpdateOrderTests {

        @Test
        void shouldUpdateExistingOrder() {

            Order order =
                    createOrder(
                            "ORD006",
                            100,
                            180.0
                    );

            UpdateOrderRequest request =
                    mock(UpdateOrderRequest.class);

            when(request.getQuantity())
                    .thenReturn(250);

            when(request.getPrice())
                    .thenReturn(190.0);

            when(orderRepository.findById("ORD006"))
                    .thenReturn(Optional.of(order));

            when(
                    exchange.modifyOrder(
                            "AAPL",
                            "ORD006",
                            250,
                            190.0
                    )
            ).thenReturn(true);

            OrderResponse response =
                    orderService.updateOrder(
                            "ORD006",
                            request
                    );

            assertNotNull(response);

            assertEquals(
                    250,
                    order.getQuantity()
            );

            assertEquals(
                    190.0,
                    order.getPrice()
            );

            verify(exchange)
                    .modifyOrder(
                            "AAPL",
                            "ORD006",
                            250,
                            190.0
                    );

            verify(orderRepository)
                    .save(order);
        }

        @Test
        void shouldThrowExceptionWhenOrderDoesNotExist() {

            UpdateOrderRequest request =
                    mock(UpdateOrderRequest.class);

            when(orderRepository.findById("UNKNOWN"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    OrderNotFoundException.class,
                    () ->
                            orderService.updateOrder(
                                    "UNKNOWN",
                                    request
                            )
            );

            verify(orderRepository)
                    .findById("UNKNOWN");

            verify(orderRepository, never())
                    .save(any(Order.class));
        }

        @Test
        void shouldThrowExceptionWhenExchangeCannotModifyOrder() {

            Order order =
                    createOrder(
                            "ORD007",
                            100,
                            200.0
                    );

            UpdateOrderRequest request =
                    mock(UpdateOrderRequest.class);

            when(request.getQuantity())
                    .thenReturn(300);

            when(request.getPrice())
                    .thenReturn(210.0);

            when(orderRepository.findById("ORD007"))
                    .thenReturn(Optional.of(order));

            when(
                    exchange.modifyOrder(
                            "AAPL",
                            "ORD007",
                            300,
                            210.0
                    )
            ).thenReturn(false);

            assertThrows(
                    OrderNotFoundException.class,
                    () ->
                            orderService.updateOrder(
                                    "ORD007",
                                    request
                            )
            );

            assertEquals(
                    100,
                    order.getQuantity()
            );

            assertEquals(
                    200.0,
                    order.getPrice()
            );

            verify(orderRepository, never())
                    .save(any(Order.class));
        }
    }

    @Nested
    class VerificationTests {

        @Test
        void constructorShouldRegisterAaplStock() {

            verify(exchange)
                    .addStock("AAPL");
        }

        @Test
        void getOrderShouldNotModifyRepository() {

            Order order =
                    createOrder(
                            "ORD008",
                            25,
                            100.0
                    );

            when(orderRepository.findById("ORD008"))
                    .thenReturn(Optional.of(order));

            orderService.getOrder("ORD008");

            verify(orderRepository)
                    .findById("ORD008");

            verify(orderRepository, never())
                    .save(any(Order.class));

            verify(orderRepository, never())
                    .deleteById(anyString());
        }

        @Test
        void getAllOrdersShouldNotModifyRepository() {

            when(orderRepository.findAll())
                    .thenReturn(List.of());

            orderService.getAllOrders();

            verify(orderRepository)
                    .findAll();

            verify(orderRepository, never())
                    .save(any(Order.class));

            verify(orderRepository, never())
                    .deleteById(anyString());
        }
    }
}