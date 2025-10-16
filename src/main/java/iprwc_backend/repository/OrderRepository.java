package iprwc_backend.repository;


import iprwc_backend.entity.Order;
import iprwc_backend.entity.OrderStatus;
import iprwc_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user
    List<Order> findByUser(User user);

    // Find orders by user ID
    List<Order> findByUserId(Long userId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by user and status
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    // Find orders created between dates
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count orders by status
    long countByStatus(OrderStatus status);

    // Get total revenue
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal getTotalRevenue();

    // Get total revenue by status
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenueByStatus(OrderStatus status);

    // Find recent orders (last N orders)
    List<Order> findTop10ByOrderByCreatedAtDesc();
}