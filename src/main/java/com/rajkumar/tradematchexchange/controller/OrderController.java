package com.rajkumar.tradematchexchange.controller;

import com.rajkumar.tradematchexchange.dto.OrderDto;
import com.rajkumar.tradematchexchange.dto.OrderRequest;
import com.rajkumar.tradematchexchange.dto.OrderResponse;
import com.rajkumar.tradematchexchange.dto.UpdateOrderRequest;
import com.rajkumar.tradematchexchange.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(
        name = "Order Management",
        description = "APIs for creating, retrieving, updating and deleting trading orders."
)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {

        this.orderService = orderService;
    }

    @Operation(
            summary = "Place a new order",
            description = "Creates a new order, validates it, stores it and sends it to the matching engine."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public OrderResponse placeOrder(

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "orderId":"ORD001",
                                              "stockSymbol":"AAPL",
                                              "quantity":100,
                                              "price":200,
                                              "orderType":"BUY",
                                              "executionType":"LIMIT"
                                            }
                                            """
                            )
                    )
            )

            @Valid @RequestBody OrderRequest request) {

        return orderService.placeOrder(request);
    }

    @Operation(
            summary = "Get all active orders",
            description = "Returns all active orders currently stored in the Order Management System."
    )
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @GetMapping
    public List<OrderDto> getAllOrders() {

        return orderService.getAllOrders();
    }

    @Operation(
            summary = "Get order by ID",
            description = "Returns a single order using its unique Order ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public OrderDto getOrder(

            @Parameter(
                    description = "Unique Order ID",
                    example = "ORD001"
            )

            @PathVariable String orderId) {

        return orderService.getOrder(orderId);
    }

    @Operation(
            summary = "Update an existing order",
            description = "Updates quantity and price of an active order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}")
    public OrderResponse updateOrder(

            @Parameter(
                    description = "Unique Order ID",
                    example = "ORD001"
            )

            @PathVariable String orderId,

            @Valid @RequestBody UpdateOrderRequest request) {

        return orderService.updateOrder(
                orderId,
                request);
    }

    @Operation(
            summary = "Delete an order",
            description = "Cancels and removes an active order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{orderId}")
    public OrderResponse deleteOrder(

            @Parameter(
                    description = "Unique Order ID",
                    example = "ORD001"
            )

            @PathVariable String orderId) {

        return orderService.deleteOrder(orderId);
    }
}