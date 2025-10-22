package iprwc_backend.repository;


import iprwc_backend.entity.OrderItem;
import iprwc_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find order items by order ID
    List<OrderItem> findByOrderId(Long orderId);

    // Find order items by product
    List<OrderItem> findByProduct(Product product);

}