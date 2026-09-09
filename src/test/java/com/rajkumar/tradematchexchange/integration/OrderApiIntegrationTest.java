package com.rajkumar.tradematchexchange.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.repository.OrderRepository;
import com.rajkumar.tradematchexchange.service.Exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tradematch-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.show-sql=false"
        }
)
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Exchange exchange;

    /**
     * Every integration test starts from a clean
     * database and clean in-memory exchange.
     *
     * Tests must never depend on another test
     * running before them.
     */
    @BeforeEach
    void resetState() {

        /*
         * Clears:
         * - in-memory order books
         * - RiskManager order IDs
         * - persisted trades
         */
        exchange.clearExchange();

        /*
         * Active orders are stored separately,
         * so clear them explicitly.
         */
        orderRepository.deleteAll();

        /*
         * clearExchange() removes all order books,
         * therefore recreate the instrument used
         * by the REST API tests.
         */
        exchange.addStock("AAPL");
    }

    /**
     * Creates a valid LIMIT BUY request.
     */
    private OrderRequest createOrderRequest(
            String orderId) {

        OrderRequest request =
                new OrderRequest();

        request.setOrderId(orderId);
        request.setStockSymbol("AAPL");
        request.setQuantity(100);
        request.setPrice(200.0);
        request.setOrderType("BUY");
        request.setExecutionType("LIMIT");

        return request;
    }

    /**
     * Places one order through the real REST API.
     *
     * Used by tests that require an existing order.
     */
    private void placeOrder(
            String orderId) throws Exception {

        OrderRequest request =
                createOrderRequest(orderId);

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
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value(orderId)
                );
    }

    @Test
    @DisplayName("Application context loads")
    void contextLoads() {
    }

    @Test
    @DisplayName("POST /orders creates an order")
    void shouldCreateOrder()
            throws Exception {

        OrderRequest request =
                createOrderRequest(
                        "CREATE-100"
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
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("CREATE-100")
                );

        /*
         * Proves that REST -> service -> JPA
         * persisted the active order.
         */
        mockMvc.perform(
                        get("/orders/CREATE-100")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    @DisplayName("GET /orders returns active orders")
    void shouldReturnOrders()
            throws Exception {

        placeOrder(
                "LIST-100"
        );

        mockMvc.perform(
                        get("/orders")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    @DisplayName("GET /orders/{id} returns an existing order")
    void shouldReturnSingleOrder()
            throws Exception {

        /*
         * This test creates its own prerequisite.
         * It does not depend on shouldCreateOrder().
         */
        placeOrder(
                "GET-100"
        );

        mockMvc.perform(
                        get("/orders/GET-100")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    @DisplayName("PUT /orders/{id} updates an existing order")
    void shouldUpdateOrder()
            throws Exception {

        /*
         * Create the order inside THIS test first.
         */
        placeOrder(
                "UPDATE-100"
        );

        String body =
                """
                {
                    "quantity": 150,
                    "price": 220.0
                }
                """;

        mockMvc.perform(
                        put("/orders/UPDATE-100")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("UPDATE-100")
                );

        /*
         * Confirm the order still exists after
         * modification.
         */
        mockMvc.perform(
                        get("/orders/UPDATE-100")
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    @DisplayName("DELETE /orders/{id} cancels an existing order")
    void shouldDeleteOrder()
            throws Exception {

        /*
         * Create the order inside THIS test first.
         */
        placeOrder(
                "DELETE-100"
        );

        mockMvc.perform(
                        delete("/orders/DELETE-100")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.orderId")
                                .value("DELETE-100")
                );

        /*
         * After cancellation the active order
         * must no longer exist.
         */
        mockMvc.perform(
                        get("/orders/DELETE-100")
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    @DisplayName("POST /orders rejects invalid request")
    void shouldRejectInvalidRequest()
            throws Exception {

        String body =
                """
                {
                    "orderId": "",
                    "stockSymbol": "",
                    "quantity": 0,
                    "price": 0,
                    "orderType": "",
                    "executionType": ""
                }
                """;

        mockMvc.perform(
                        post("/orders")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                );
    }

    @Test
    @DisplayName("GET /orders/{id} returns 404 for unknown order")
    void shouldReturn404ForUnknownOrder()
            throws Exception {

        mockMvc.perform(
                        get("/orders/UNKNOWN")
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Order Not Found")
                );
    }
}