package com.rajkumar.tradematchexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /orders")
    void shouldPlaceOrder() throws Exception {

        OrderRequest request = new OrderRequest();
        request.setOrderId("ORD001");
        request.setStockSymbol("AAPL");
        request.setQuantity(100);
        request.setPrice(200);
        request.setOrderType("BUY");
        request.setExecutionType("LIMIT");

        OrderResponse response =
                new OrderResponse("SUCCESS","ORD001","Order placed successfully.");

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.orderId").value("ORD001"));
    }

    @Test
    @DisplayName("GET /orders")
    void shouldReturnAllOrders() throws Exception {

        OrderDto dto = new OrderDto();
        when(orderService.getAllOrders()).thenReturn(List.of(dto));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /orders/{id}")
    void shouldReturnOrder() throws Exception {

        OrderDto dto = new OrderDto();

        when(orderService.getOrder("ORD001"))
                .thenReturn(dto);

        mockMvc.perform(get("/orders/ORD001"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /orders/{id}")
    void shouldUpdateOrder() throws Exception {

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setQuantity(150);
        request.setPrice(220);

        OrderResponse response =
                new OrderResponse("SUCCESS","ORD001","Order updated successfully.");

        when(orderService.updateOrder(eq("ORD001"), any(UpdateOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/orders/ORD001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /orders/{id}")
    void shouldDeleteOrder() throws Exception {

        OrderResponse response =
                new OrderResponse("SUCCESS","ORD001","Order cancelled successfully.");

        when(orderService.deleteOrder("ORD001"))
                .thenReturn(response);

        mockMvc.perform(delete("/orders/ORD001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
