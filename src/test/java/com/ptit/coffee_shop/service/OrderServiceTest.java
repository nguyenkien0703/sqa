package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.OrderStatus;
import com.ptit.coffee_shop.common.enums.PaymentMethod;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.*;
import com.ptit.coffee_shop.payload.request.OrderItemRequest;
import com.ptit.coffee_shop.payload.request.OrderRequest;
import com.ptit.coffee_shop.payload.response.OrderItemResponse;
import com.ptit.coffee_shop.payload.response.OrderResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductItemRepository productItemRepository;
    @Mock
    private ShippingAddressRepository shippingAddressRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageBuilder messageBuilder;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private ProductItem testProductItem;
    private Product testProduct;
    private TypeProduct testType;
    private ShippingAddress testShippingAddress;
    private User testUser;
    private Image testImage;
    private Transaction testTransaction;
    private RespMessage successResponse;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Set up test product type
        testType = new TypeProduct();
        testType.setId(1L);
        testType.setName("Hot Coffee");
        testType.setStatus(Status.ACTIVE);

        // Set up test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Coffee");
        testProduct.setStatus(Status.ACTIVE);

        // Set up test product item
        testProductItem = new ProductItem();
        testProductItem.setId(1L);
        testProductItem.setProduct(testProduct);
        testProductItem.setType(testType);
        testProductItem.setStock(10);

        // Set up test shipping address
        testShippingAddress = new ShippingAddress();
        testShippingAddress.setId(1L);
        testShippingAddress.setStatus(Status.ACTIVE);
        testShippingAddress.setUser(testUser);
        testShippingAddress.setReceiverName("Test User");
        testShippingAddress.setReceiverPhone("1234567890");
        testShippingAddress.setLocation("Test Address");

        // Set up test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderDate(new Date());
        testOrder.setStatus(OrderStatus.Processing);
        testOrder.setPaymentMethod(PaymentMethod.COD);
        testOrder.setShippingAddress(testShippingAddress);

        // Set up test order item
        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProductItem(testProductItem);
        testOrderItem.setPrice(100000.0);
        testOrderItem.setAmount(2);
        testOrderItem.setDiscount(10000.0);

        // Set up test image
        testImage = new Image();
        testImage.setUrl("http://test-image.jpg");
        testImage.setProduct(testProduct);

        // Set up test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setOrder(testOrder);

        // Set up success response
        successResponse = RespMessage.builder()
                .data("Test Response")
                .build();

        // Set up security context
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Default mock behaviors
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(imageRepository.findByProduct(testProduct)).thenReturn(Arrays.asList(testImage));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenGetAllOrders_thenReturnOrderList() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        List<OrderItem> orderItems = Arrays.asList(testOrderItem);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(orderItems);

        // Act
        RespMessage result = orderService.getAllOrders();

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findAll();
    }

    @Test
    public void whenGetOrderById_withValidId_thenReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        // Act
        RespMessage result = orderService.getOrderById(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
    }

    @Test
    public void whenAddOrder_withValidData_thenReturnSuccess() {
        // Arrange
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductItemId(1L);
        orderItemRequest.setAmount(2);
        orderItemRequest.setPrice(100000.0);
        orderItemRequest.setDiscount(10000.0);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddressId(1L);
        orderRequest.setPaymentMethod(PaymentMethod.COD);
        orderRequest.setOrderItems(Arrays.asList(orderItemRequest));

        when(shippingAddressRepository.findById(1L)).thenReturn(Optional.of(testShippingAddress));
        when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        RespMessage result = orderService.addOrder(orderRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).save(any());
        verify(orderItemRepository).save(any());
    }

    @Test
    public void whenUpdateOrderStatus_withValidStatus_thenReturnSuccess() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        RespMessage result = orderService.updateOrderStatus(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.Processed);
    }

    @Test
    public void whenCancelOrder_withProcessingOrder_thenReturnSuccess() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        // Act
        RespMessage result = orderService.cancelOrder(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.Cancelled);
    }

    @Test
    public void whenGetOrdersByUser_thenReturnUserOrders() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        // Act
        RespMessage result = orderService.getOrdersByUser();

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    public void whenGetOrderByStatus_thenReturnFilteredOrders() {
        // Arrange
        when(orderRepository.findByStatus(OrderStatus.Processing)).thenReturn(Arrays.asList(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(testOrderItem));

        // Act
        RespMessage result = orderService.getOrderByStatus(OrderStatus.Processing);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findByStatus(OrderStatus.Processing);
    }

    @Test
    public void whenToOrderItemResponse_thenReturnResponse() {
        // Arrange
        when(imageRepository.findByProduct(testProduct)).thenReturn(Arrays.asList(testImage));

        // Act
        OrderItemResponse result = orderService.toOrderItemResponse(testOrderItem);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductName()).isEqualTo(testProduct.getName());
        assertThat(result.getProductImage()).isEqualTo(testImage.getUrl());
    }



} 