package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.model.OrderItem;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @InjectMocks
    private OrderItemService orderItemService;

    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private RespMessage successResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        orderItem1.setAmount(2);
        orderItem1.setPrice(100.0);
        orderItem1.setDiscount(10.0);

        orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        orderItem2.setAmount(3);
        orderItem2.setPrice(150.0);
        orderItem2.setDiscount(15.0);

        // Setup success response
        successResponse = RespMessage.builder()
                .respCode("00")
                .respDesc("Success")
                .build();
    }

    @Nested
    @DisplayName("Test getAllOrderItems")
    class GetAllOrderItemsTest {
        @Test
        @DisplayName("Lấy danh sách order items thành công khi có dữ liệu")
        void getAllOrderItems_Success_WithData() {
            // Input: Danh sách order items có dữ liệu
            // Expected: Trả về RespMessage chứa danh sách order items

            // Arrange
            List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);
            when(orderItemRepository.findAll()).thenReturn(orderItems);
            when(messageBuilder.buildSuccessMessage(orderItems)).thenReturn(successResponse);

            // Act
            RespMessage result = orderItemService.getAllOrderItems();

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            verify(orderItemRepository, times(1)).findAll();
            verify(messageBuilder, times(1)).buildSuccessMessage(orderItems);
        }

        @Test
        @DisplayName("Lấy danh sách order items thành công khi không có dữ liệu")
        void getAllOrderItems_Success_EmptyList() {
            // Input: Danh sách order items rỗng
            // Expected: Trả về RespMessage chứa danh sách rỗng

            // Arrange
            List<OrderItem> emptyList = Collections.emptyList();
            when(orderItemRepository.findAll()).thenReturn(emptyList);
            when(messageBuilder.buildSuccessMessage(emptyList)).thenReturn(successResponse);

            // Act
            RespMessage result = orderItemService.getAllOrderItems();

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            verify(orderItemRepository, times(1)).findAll();
            verify(messageBuilder, times(1)).buildSuccessMessage(emptyList);
        }
    }
}