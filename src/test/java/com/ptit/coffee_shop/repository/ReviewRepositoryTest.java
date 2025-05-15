package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.OrderStatus;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * TC1: Test findByProductId() trả về danh sách các review của sản phẩm theo productId.
     * Input: Tạo product, productItem, orderItem, review với status = 'ACTIVE' cho productId
     * Expected Output: Danh sách reviews có productId tương ứng với product đã tạo
     */
    @Test
    @Transactional
    void TC1_testFindByProductId() {
        // Setup Product and ProductItem
        Product product = new Product();
        product.setName("Mocha");
        productRepository.save(product);

        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItemRepository.save(productItem);

        // Setup Order and OrderItem
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.Completed);
        orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductItem(productItem);
        orderItemRepository.save(orderItem);

        // Setup Review with ACTIVE status for the productId
        Review review = new Review();
        review.setOrderItem(orderItem);
        review.setStatus(Status.ACTIVE);
        review.setRating(5);
        review.setComment("Excellent product!");
        reviewRepository.save(review);

        // Input: Tìm review của sản phẩm theo productId
        List<Review> reviews = reviewRepository.findByProductId(product.getId());

        // Expected Output: Review list phải chứa review của productId vừa tạo
        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        assertEquals(product.getId(), reviews.get(0).getOrderItem().getProductItem().getProduct().getId());
    }

    /**
     * TC2: Test findByOrderId() trả về danh sách các review của orderId.
     * Input: Tạo order, orderItem, review với orderId
     * Expected Output: Danh sách reviews có orderId tương ứng với order đã tạo
     */
    @Test
    @Transactional
    void TC2_testFindByOrderId() {
        // Setup Product, ProductItem, Order, OrderItem
        Product product = new Product();
        product.setName("Latte");
        productRepository.save(product);

        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItemRepository.save(productItem);

        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.Completed);
        orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductItem(productItem);
        orderItemRepository.save(orderItem);

        // Setup Review with orderId
        Review review = new Review();
        review.setOrderItem(orderItem);
        review.setStatus(Status.ACTIVE);
        review.setRating(4);
        review.setComment("Good, but could be better");
        reviewRepository.save(review);

        // Input: Tìm review của đơn hàng theo orderId
        List<Review> reviews = reviewRepository.findByOrderId(order.getId());

        // Expected Output: Review list phải chứa review của orderId vừa tạo
        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        assertEquals(order.getId(), reviews.get(0).getOrderItem().getOrder().getId());
    }
}
