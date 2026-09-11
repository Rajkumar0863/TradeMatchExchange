package com.rajkumar.tradematchexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.exception.DuplicateOrderException;
import com.rajkumar.tradematchexchange.exception.OrderNotFoundException;
import com.rajkumar.tradematchexchange.service.OrderService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    /*
     * ---------------------------------------------------------
     * SUCCESSFUL REQUEST TESTS
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("POST /orders returns 200 when order is placed")
    void shouldPlaceOrder() throws Exception {

        OrderRequest request = validOrderRequest();

        OrderResponse response =
                new OrderResponse(
                        "SUCCESS",
                        "ORD001",
                        "Order placed successfully."
                );

        when(orderService.placeOrder(
                any(OrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("ORD001")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order placed successfully."
                                )
                );
    }

    @Test
    @DisplayName("GET /orders returns 200")
    void shouldReturnAllOrders() throws Exception {

        OrderDto dto = new OrderDto();

        when(orderService.getAllOrders())
                .thenReturn(List.of(dto));

        mockMvc.perform(
                        get("/orders")
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /orders/{id} returns 200 when order exists")
    void shouldReturnOrder() throws Exception {

        OrderDto dto = new OrderDto();

        when(orderService.getOrder("ORD001"))
                .thenReturn(dto);

        mockMvc.perform(
                        get("/orders/ORD001")
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /orders/{id} returns 200 when order is updated")
    void shouldUpdateOrder() throws Exception {

        UpdateOrderRequest request =
                validUpdateRequest();

        OrderResponse response =
                new OrderResponse(
                        "SUCCESS",
                        "ORD001",
                        "Order updated successfully."
                );

        when(orderService.updateOrder(
                eq("ORD001"),
                any(UpdateOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/orders/ORD001")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("ORD001")
                );
    }

    @Test
    @DisplayName("DELETE /orders/{id} returns 200 when order is cancelled")
    void shouldDeleteOrder() throws Exception {

        OrderResponse response =
                new OrderResponse(
                        "SUCCESS",
                        "ORD001",
                        "Order cancelled successfully."
                );

        when(orderService.deleteOrder("ORD001"))
                .thenReturn(response);

        mockMvc.perform(
                        delete("/orders/ORD001")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("ORD001")
                );
    }

    /*
     * ---------------------------------------------------------
     * 400 BAD REQUEST TESTS
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("POST /orders returns real HTTP 400 for invalid request")
    void shouldReturnBadRequestForInvalidOrder()
            throws Exception {

        OrderRequest request =
                validOrderRequest();

        request.setQuantity(0);

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Validation Failed")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/orders")
                );
    }

    @Test
    @DisplayName("POST /orders returns real HTTP 400 for malformed JSON")
    void shouldReturnBadRequestForMalformedJson()
            throws Exception {

        String malformedJson = """
                {
                    "orderId": "ORD001",
                    "stockSymbol": "AAPL",
                    "quantity": 100,
                    "price": 200,
                    "orderType": "BUY",
                    "executionType": "LIMIT"
                """;

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(malformedJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Invalid Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Malformed JSON or invalid enum value."
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/orders")
                );
    }

    @Test
    @DisplayName("POST /orders returns real HTTP 400 for business validation failure")
    void shouldReturnBadRequestForIllegalArgument()
            throws Exception {

        OrderRequest request =
                validOrderRequest();

        when(orderService.placeOrder(
                any(OrderRequest.class)))
                .thenThrow(
                        new IllegalArgumentException(
                                "Risk Validation Failed."
                        )
                );

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Invalid Order")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Risk Validation Failed."
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/orders")
                );
    }

    /*
     * ---------------------------------------------------------
     * 404 NOT FOUND TESTS
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("GET /orders/{id} returns real HTTP 404 when order does not exist")
    void shouldReturnNotFoundWhenGettingMissingOrder()
            throws Exception {

        when(orderService.getOrder("MISSING"))
                .thenThrow(
                        new OrderNotFoundException(
                                "MISSING"
                        )
                );

        mockMvc.perform(
                        get("/orders/MISSING")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Order Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order not found with ID: MISSING"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/orders/MISSING"
                                )
                );
    }

    @Test
    @DisplayName("PUT /orders/{id} returns real HTTP 404 when order does not exist")
    void shouldReturnNotFoundWhenUpdatingMissingOrder()
            throws Exception {

        UpdateOrderRequest request =
                validUpdateRequest();

        when(orderService.updateOrder(
                eq("MISSING"),
                any(UpdateOrderRequest.class)))
                .thenThrow(
                        new OrderNotFoundException(
                                "MISSING"
                        )
                );

        mockMvc.perform(
                        put("/orders/MISSING")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Order Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order not found with ID: MISSING"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/orders/MISSING"
                                )
                );
    }

    @Test
    @DisplayName("DELETE /orders/{id} returns real HTTP 404 when order does not exist")
    void shouldReturnNotFoundWhenDeletingMissingOrder()
            throws Exception {

        when(orderService.deleteOrder("MISSING"))
                .thenThrow(
                        new OrderNotFoundException(
                                "MISSING"
                        )
                );

        mockMvc.perform(
                        delete("/orders/MISSING")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Order Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Order not found with ID: MISSING"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/orders/MISSING"
                                )
                );
    }

    /*
     * ---------------------------------------------------------
     * 409 CONFLICT TEST
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("POST /orders returns real HTTP 409 for duplicate active order ID")
    void shouldReturnConflictForDuplicateOrder()
            throws Exception {

        OrderRequest request =
                validOrderRequest();

        when(orderService.placeOrder(
                any(OrderRequest.class)))
                .thenThrow(
                        new DuplicateOrderException(
                                "ORD001"
                        )
                );

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Duplicate Order")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "An active order already exists with ID: ORD001"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/orders")
                );
    }

    /*
     * ---------------------------------------------------------
     * 500 INTERNAL SERVER ERROR TEST
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("GET /orders returns real HTTP 500 for unexpected failure")
    void shouldReturnInternalServerError()
            throws Exception {

        when(orderService.getAllOrders())
                .thenThrow(
                        new RuntimeException(
                                "Database unavailable"
                        )
                );

        mockMvc.perform(
                        get("/orders")
                )
                .andExpect(
                        status().isInternalServerError()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(500)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Internal Server Error"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "An unexpected error occurred."
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/orders")
                );
    }

    /*
     * ---------------------------------------------------------
     * TEST DATA HELPERS
     * ---------------------------------------------------------
     */

    private OrderRequest validOrderRequest() {

        OrderRequest request =
                new OrderRequest();

        request.setOrderId("ORD001");
        request.setStockSymbol("AAPL");
        request.setQuantity(100);
        request.setPrice(200);
        request.setOrderType("BUY");
        request.setExecutionType("LIMIT");

        return request;
    }

    private UpdateOrderRequest validUpdateRequest() {

        UpdateOrderRequest request =
                new UpdateOrderRequest();

        request.setQuantity(150);
        request.setPrice(220);

        return request;
    }
}