package com.gizmohub.services;

import com.gizmohub.dto.OrderLine;
import com.gizmohub.dto.OrderResponse;
import com.gizmohub.models.Order;
import com.gizmohub.models.OrderItem;
import com.gizmohub.models.OrderStatus;
import com.gizmohub.repositories.OrderRepository;
import com.gizmohub.repositories.ProductRepository;
import com.gizmohub.validation.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Order history and order status (Team Member 4): ORDER-11, ORDER-12. */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /** ORDER-12: a customer only ever sees their own orders. */
    @Transactional(readOnly = true)
    public List<OrderResponse> history(Long userId) {
        return toResponses(orderRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId));
    }

    /** ORDER-12: another customer's order id answers 404 (we do not reveal that it exists). */
    @Transactional(readOnly = true)
    public OrderResponse getOwnOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(OrderService::toResponse)
                .orElseThrow(() -> BusinessException.notFound("ORDER-12", "Order " + orderId + " was not found."));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return toResponses(orderRepository.findAllByOrderByCreatedAtDescIdDesc());
    }

    /**
     * ORDER-11 (admin): PLACED -> SHIPPED -> DELIVERED, or PLACED -> CANCELLED.
     * Cancelling puts the ordered quantities back into stock (products deleted since are skipped).
     */
    @Transactional
    public OrderResponse changeStatus(Long orderId, String newStatusText) {
        OrderStatus newStatus = parseStatus(newStatusText);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER-11", "Order " + orderId + " was not found."));
        if (!isAllowedTransition(order.getStatus(), newStatus)) {
            throw BusinessException.conflict("ORDER-11",
                    "Cannot change order status from " + order.getStatus() + " to " + newStatus + ".");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productRepository.findById(item.getProductId())
                        .ifPresent(product -> product.setStock(product.getStock() + item.getQuantity()));
            }
        }
        order.setStatus(newStatus);
        return toResponse(order);
    }

    public static boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        return (from == OrderStatus.PLACED && (to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED))
                || (from == OrderStatus.SHIPPED && to == OrderStatus.DELIVERED);
    }

    static OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw BusinessException.badRequest("ORDER-11", "Status is required.");
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException unknown) {
            throw BusinessException.badRequest("ORDER-11", "Unknown status: " + status.trim());
        }
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderLine> lines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            lines.add(new OrderLine(item.getProductId(), item.getProductName(), item.getUnitPrice(),
                    item.getQuantity(), item.getLineTotal()));
        }
        return new OrderResponse(order.getId(), order.getUserId(), order.getCreatedAt(), order.getStatus().name(),
                order.getShippingAddress(), order.getPhone(), order.getSubtotal(), order.getShippingFee(),
                order.getTotal(), lines);
    }

    private static List<OrderResponse> toResponses(List<Order> orders) {
        List<OrderResponse> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(toResponse(order));
        }
        return result;
    }
}
