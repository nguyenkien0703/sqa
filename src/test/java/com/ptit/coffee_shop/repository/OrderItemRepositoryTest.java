package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.OrderStatus;
import com.ptit.coffee_shop.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    /**
     * ✅ TC1: Test findByOrderId() trả về các OrderItem đúng với orderId.
     * Input: Tạo 1 order và 1 orderItem gắn với order đó
     * Expected Output: Kết quả trả về list 1 phần tử và phần tử đó có orderId trùng với order vừa tạo
     */
    @Test
    @Transactional
    @DisplayName("findByOrderId() trả về các OrderItem theo orderId")
    void testFindByOrderId() {
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.Completed);
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setAmount(2);
        item.setPrice(100.0);
        item.setDiscount(10.0);
        orderItemRepository.save(item);

        List<OrderItem> result = orderItemRepository.findByOrderId(order.getId());
        assertEquals(1, result.size());
        assertEquals(order.getId(), result.get(0).getOrder().getId());
    }

    /**
     * ✅ TC2: Test findTop5MonthlySellingProducts() trả về các sản phẩm bán chạy trong tháng.
     * Input: Tạo 1 product, 1 productItem, 1 order, 1 orderItem với amount > 0 trong khoảng ngày hiện tại
     * Expected Output: Danh sách kết quả không rỗng (contains sản phẩm vừa tạo)
     */
    @Test
    @Transactional
    @DisplayName("findTop5MonthlySellingProducts() trả về các sản phẩm bán chạy trong tháng")
    void testFindTop5MonthlySellingProducts() {
        Product product = new Product();
        product.setName("Espresso");
        productRepository.save(product);

        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItemRepository.save(productItem);

        Order order = new Order();
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.Completed);
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductItem(productItem);
        item.setAmount(10);
        item.setPrice(50.0);
        item.setDiscount(5.0);
        orderItemRepository.save(item);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date endDate = calendar.getTime();

        List<Object[]> results = orderItemRepository.findTop5MonthlySellingProducts(startDate, endDate, PageRequest.of(0, 5));
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    /**
     * ✅ TC3: Test findTop5BestSellingProducts() trả về các sản phẩm bán chạy nhất tổng thể.
     * Input: Tạo product, productItem, order, orderItem với amount > 0
     * Expected Output: Danh sách kết quả không rỗng (contains sản phẩm vừa tạo)
     */
    @Test
    @Transactional
    @DisplayName("findTop5BestSellingProducts() trả về các sản phẩm bán chạy nhất")
    void testFindTop5BestSellingProducts() {
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

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductItem(productItem);
        item.setAmount(5);
        item.setPrice(60.0);
        item.setDiscount(10.0);
        orderItemRepository.save(item);

        List<Object[]> result = orderItemRepository.findTop5BestSellingProducts(PageRequest.of(0, 5));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * ✅ TC4: Test findTop5BestCustomers() trả về những khách hàng chi tiêu nhiều nhất.
     * Input: Tạo user, address, order, orderItem với chi tiêu cao
     * Expected Output: user vừa tạo đứng đầu danh sách kết quả
     */
    @Test
    @Transactional
    @DisplayName("findTop5BestCustomers() trả về khách hàng chi tiêu nhiều nhất")
    void testFindTop5BestCustomers() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@gmail.com");
        user.setPassword("password");
        userRepository.save(user);

        ShippingAddress address = new ShippingAddress();
        address.setUser(user);
        shippingAddressRepository.save(address);

        Order order = new Order();
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.Completed);
        order.setOrderDate(new Date());
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setAmount(3);
        item.setPrice(100.0);
        item.setDiscount(0.0);
        orderItemRepository.save(item);

        List<Object[]> result = orderItemRepository.findTop5BestCustomers();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(user.getId(), ((User) result.get(0)[0]).getId());
    }

    /**
     * ✅ TC5: Test findTop5MonthlyCustomers() trả về khách hàng chi tiêu nhiều nhất trong tháng.
     * Input: Tạo user, address, order, orderItem với chi tiêu tháng này
     * Expected Output: user vừa tạo đứng đầu danh sách kết quả
     */
    @Test
    @Transactional
    @DisplayName("findTop5MonthlyCustomers() trả về khách hàng chi tiêu nhiều trong tháng")
    void testFindTop5MonthlyCustomers() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        User user = new User();
        user.setName("Jane Smith");
        user.setEmail("jane.smith@gmail.com");
        user.setPassword("password");
        userRepository.save(user);

        ShippingAddress address = new ShippingAddress();
        address.setUser(user);
        shippingAddressRepository.save(address);

        Order order = new Order();
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.Completed);
        order.setOrderDate(new Date());
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setAmount(7);
        item.setPrice(30.0);
        item.setDiscount(5.0);
        orderItemRepository.save(item);

        List<Object[]> result = orderItemRepository.findTop5MonthlyCustomers(month, year, PageRequest.of(0, 5));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(user.getId(), ((User) result.get(0)[0]).getId());
    }

    /**
     * ✅ TC6: Test findTotalSold() trả về tổng số lượng sản phẩm đã bán.
     * Input: Tạo product, productItem, order, orderItem với amount = 15
     * Expected Output: Tổng sold trả về là 15
     */
    @Test
    @Transactional
    @DisplayName("findTotalSold() trả về tổng số lượng đã bán của 1 sản phẩm")
    void testFindTotalSold() {
        Product product = new Product();
        product.setName("Cappuccino");
        productRepository.save(product);

        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItemRepository.save(productItem);

        Order order = new Order();
        order.setStatus(OrderStatus.Completed);
        order.setOrderDate(new Date());
        orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductItem(productItem);
        item.setAmount(15);
        item.setPrice(70.0);
        item.setDiscount(0.0);
        orderItemRepository.save(item);

        Optional<Integer> totalSold = orderItemRepository.findTotalSold(product.getId());
        assertTrue(totalSold.isPresent());
        assertEquals(15, totalSold.get());
    }
}