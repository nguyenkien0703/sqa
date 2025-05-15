package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Order;
import com.ptit.coffee_shop.model.Transaction;
import com.ptit.coffee_shop.payload.request.TransactionRequest;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.OrderRepository;
import com.ptit.coffee_shop.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @InjectMocks
    private TransactionService transactionService;

    private Order testOrder;
    private Transaction testTransaction;
    private TransactionRequest testRequest;
    private RespMessage successResponse;

    @BeforeEach
    void setUp() {
        // Setup test order
        testOrder = new Order();
        testOrder.setId(1L);

        // Setup test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setOrder(testOrder);
        testTransaction.setAmount(100.0);
        testTransaction.setTransactionNo("TXN123");
        testTransaction.setCommand("pay");
        testTransaction.setPayDate(new Date());
        testTransaction.setTxnRef("REF123");

        // Setup test request
        testRequest = new TransactionRequest();
        testRequest.setOrderId(1L);
        testRequest.setAmount(100.0);
        testRequest.setTransactionNo("TXN123");
        testRequest.setPayDate(new Date());
        testRequest.setTxnRef("REF123");

        // Setup success response
        successResponse = RespMessage.builder()
                .respCode("00")
                .respDesc("Success")
                .build();
    }

    @Nested
    @DisplayName("Test addTransaction")
    class AddTransactionTest {
        @Test
        @DisplayName("TC01 - Thêm transaction thành công")
        void addTransaction_Success() {
            // Input: TransactionRequest hợp lệ
            // Expected: Trả về RespMessage chứa thông tin transaction đã tạo

            // Arrange
            when(orderRepository.findById(testRequest.getOrderId())).thenReturn(Optional.of(testOrder));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

            // Act
            RespMessage result = transactionService.addTransaction(testRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            verify(orderRepository, times(1)).findById(testRequest.getOrderId());
            verify(transactionRepository, times(1)).save(any(Transaction.class));
            verify(messageBuilder, times(1)).buildSuccessMessage(any());
        }

        @Test
        @DisplayName("TC02 - Thêm transaction thất bại khi không tìm thấy order")
        void addTransaction_OrderNotFound() {
            // Input: TransactionRequest với orderId không tồn tại
            // Expected: Ném ra CoffeeShopException với mã lỗi NOT_FOUND

            // Arrange
            when(orderRepository.findById(testRequest.getOrderId())).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> transactionService.addTransaction(testRequest));
            assertEquals(Constant.NOT_FOUND, exception.getCode());
            verify(orderRepository, times(1)).findById(testRequest.getOrderId());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC03 - Thêm transaction thất bại khi lưu transaction thất bại")
        void addTransaction_SaveFailed() {
            // Input: TransactionRequest hợp lệ
            // Expected: Ném ra CoffeeShopException với mã lỗi UNDEFINED

            // Arrange
            when(orderRepository.findById(testRequest.getOrderId())).thenReturn(Optional.of(testOrder));
            when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Save failed"));

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> transactionService.addTransaction(testRequest));
            assertEquals(Constant.UNDEFINED, exception.getCode());
            verify(orderRepository, times(1)).findById(testRequest.getOrderId());
            verify(transactionRepository, times(1)).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Test getTransaction")
    class GetTransactionTest {
        @Test
        @DisplayName("TC04 - Lấy transaction thành công")
        void getTransaction_Success() {
            // Input: orderId hợp lệ
            // Expected: Trả về RespMessage chứa thông tin transaction

            // Arrange
            when(transactionRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testTransaction));
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

            // Act
            RespMessage result = transactionService.getTransaction(testOrder.getId());

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            verify(transactionRepository, times(1)).findByOrderId(testOrder.getId());
            verify(messageBuilder, times(1)).buildSuccessMessage(any());
        }

        @Test
        @DisplayName("TC05 - Lấy transaction thất bại khi không tìm thấy transaction")
        void getTransaction_NotFound() {
            // Input: orderId không có transaction
            // Expected: Ném ra CoffeeShopException với mã lỗi NOT_FOUND

            // Arrange
            when(transactionRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> transactionService.getTransaction(testOrder.getId()));
            assertEquals(Constant.NOT_FOUND, exception.getCode());
            verify(transactionRepository, times(1)).findByOrderId(testOrder.getId());
        }
    }
}