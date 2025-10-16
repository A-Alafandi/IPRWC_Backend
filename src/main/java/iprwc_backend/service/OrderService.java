package iprwc_backend.service;

import iprwc_backend.dto.DashboardStats;
import iprwc_backend.dto.request.OrderItemRequest;
import iprwc_backend.dto.request.OrderRequest;
import iprwc_backend.dto.response.OrderItemResponse;
import iprwc_backend.dto.response.OrderResponse;
import iprwc_backend.dto.response.ProductResponse;
import iprwc_backend.dto.response.UserResponse;
import iprwc_backend.entity.*;
import iprwc_backend.exception.ResourceNotFoundException;
import iprwc_backend.repository.OrderRepository;
import iprwc_backend.repository.ProductRepository;
import iprwc_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // Get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get order by ID
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToResponse(order);
    }

    // Get orders by user ID
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get orders by status
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Create order
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            // Check stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.addItem(orderItem);

            // Calculate total
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Update product stock
            productService.updateStock(product.getId(), itemRequest.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }

    // Update order status
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    // Get dashboard statistics
    public DashboardStats getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long processingOrders = orderRepository.countByStatus(OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new DashboardStats(
                totalProducts,
                totalOrders,
                totalUsers,
                pendingOrders,
                processingOrders,
                shippedOrders,
                deliveredOrders,
                totalRevenue
        );
    }

    // Convert entity to response DTO
    private OrderResponse convertToResponse(Order order) {
        UserResponse userResponse = new UserResponse(
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getUser().getFirstName(),
                order.getUser().getLastName(),
                order.getUser().getRole(),
                order.getUser().getAddress(),
                order.getUser().getCity(),
                order.getUser().getState(),
                order.getUser().getZipCode(),
                order.getUser().getCountry(),
                order.getUser().getPhoneNumber(),
                order.getUser().getCreatedAt()
        );

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::convertOrderItemToResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                userResponse,
                itemResponses,
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse convertOrderItemToResponse(OrderItem item) {
        ProductResponse productResponse = new ProductResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getDescription(),
                item.getProduct().getPrice(),
                item.getProduct().getCategory(),
                item.getProduct().getImage(),
                item.getProduct().getStock(),
                item.getProduct().getCreatedAt(),
                item.getProduct().getUpdatedAt()
        );

        return new OrderItemResponse(
                item.getId(),
                productResponse,
                item.getQuantity(),
                item.getPrice()
        );
    }
}