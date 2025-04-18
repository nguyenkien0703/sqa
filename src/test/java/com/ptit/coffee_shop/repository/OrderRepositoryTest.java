package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.OrderStatus;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Order;
import com.ptit.coffee_shop.model.ShippingAddress;
import com.ptit.coffee_shop.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Test
    public void whenFindByShippingAddressId_thenReturnOrder() {
        // Tạo dữ liệu test: User
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        userRepository.save(user);

        // Tạo dữ liệu test: ShippingAddress
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setReceiverName("Test User");
        shippingAddress.setReceiverPhone("0123456789");
        shippingAddress.setLocation("123 Test Street");
        shippingAddress.setStatus(Status.ACTIVE);
        shippingAddress.setUser(user);
        shippingAddressRepository.save(shippingAddress);

        // Tạo dữ liệu test: Order
        Order order = new Order();
        order.setStatus(OrderStatus.Processing);
        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);

        // Thực hiện test tìm kiếm order theo shipping address id
        Optional<Order> found = orderRepository.findByShippingAddressId(shippingAddress.getId());

        // Kiểm tra kết quả
        assertThat(found).isPresent();
        assertThat(found.get().getShippingAddress().getId()).isEqualTo(shippingAddress.getId());
    }

    @Test
    public void whenFindByUserId_thenReturnOrders() {
        // Tạo dữ liệu test: User
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        // Tạo dữ liệu test: ShippingAddress
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setReceiverName("Test User");
        shippingAddress.setReceiverPhone("0123456789");
        shippingAddress.setLocation("123 Test Street");
        shippingAddress.setStatus(Status.ACTIVE);
        shippingAddress.setUser(user);
        shippingAddressRepository.save(shippingAddress);

        // Tạo dữ liệu test: Nhiều Order cho cùng một user
        Order order1 = new Order();
        order1.setStatus(OrderStatus.Processing);
        order1.setShippingAddress(shippingAddress);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setStatus(OrderStatus.Completed);
        order2.setShippingAddress(shippingAddress);
        orderRepository.save(order2);

        // Thực hiện test tìm kiếm orders theo user id
        List<Order> foundOrders = orderRepository.findByUserId(user.getId());

        // Kiểm tra kết quả
        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders).allMatch(order -> 
            order.getShippingAddress().getUser().getId() == user.getId()
        );
    }

    @Test
    public void whenFindByStatus_thenReturnOrders() {
        // Tạo dữ liệu test: User và ShippingAddress
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        userRepository.save(user);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setReceiverName("Test User");
        shippingAddress.setReceiverPhone("0123456789");
        shippingAddress.setLocation("123 Test Street");
        shippingAddress.setStatus(Status.ACTIVE);
        shippingAddress.setUser(user);
        shippingAddressRepository.save(shippingAddress);

        // Tạo dữ liệu test: Các Order với status khác nhau
        Order processingOrder = new Order();
        processingOrder.setStatus(OrderStatus.Processing);
        processingOrder.setShippingAddress(shippingAddress);
        orderRepository.save(processingOrder);

        Order completedOrder = new Order();
        completedOrder.setStatus(OrderStatus.Completed);
        completedOrder.setShippingAddress(shippingAddress);
        orderRepository.save(completedOrder);

        // Thực hiện test tìm kiếm orders theo status
        List<Order> processingOrders = orderRepository.findByStatus(OrderStatus.Processing);

        // Kiểm tra kết quả
        assertThat(processingOrders).hasSize(1);
        assertThat(processingOrders.get(0).getStatus() == OrderStatus.Processing).isTrue();
    }


    
}
