package com.ptit.coffee_shop.service;

import com.google.gson.JsonObject;
import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.OrderStatus;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.config.OnlinePaymentConfig;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Order;
import com.ptit.coffee_shop.model.ShippingAddress;
import com.ptit.coffee_shop.model.Transaction;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.response.PaymentResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.OrderRepository;
import com.ptit.coffee_shop.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.view.RedirectView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnlinePaymentServiceTest {

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OnlinePaymentService onlinePaymentService;

    private Order order;
    private Transaction transaction;
    private PaymentResponse paymentResponse;
    private RespMessage respMessage;

    @BeforeEach
    void setUp() {
        // Initialize test data
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.Processing);
        order.setShippingAddress(new ShippingAddress());
        order.getShippingAddress().setUser(new User());
        order.getShippingAddress().getUser().setEmail("test@example.com");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setOrder(order);
        transaction.setTxnRef("12345678");
        transaction.setTransactionNo("TXN123");
        transaction.setAmount(100000);
        transaction.setPayDate(new Date());

        paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("OK");
        paymentResponse.setMessage("Successfully created payment");
        paymentResponse.setURL("https://vnpay.vn/payment");

        respMessage = new RespMessage();
        respMessage.setRespCode(Constant.SUCCESS);
        respMessage.setRespDesc("Success");
        respMessage.setData(paymentResponse);

        // Set field values for testing
        setField(onlinePaymentService, "frontEndUrl", "http://frontend.com");
        setField(onlinePaymentService, "backEndUrl", "http://backend.com");
    }

    // region createVNPayPayment
    @Test
    void createVNPayPayment_WithValidAmount_ShouldReturnPaymentUrl() {
        // Arrange
        int amount = 1000;
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(messageBuilder.buildSuccessMessage(any(PaymentResponse.class))).thenReturn(respMessage);

        // Mock static method in OnlinePaymentConfig
        try (var mockedStatic = mockStatic(OnlinePaymentConfig.class)) {
            mockedStatic.when(() -> OnlinePaymentConfig.getRandomNumber(8)).thenReturn("12345678");
            mockedStatic.when(() -> OnlinePaymentConfig.getIpAddress(request)).thenReturn("127.0.0.1");
            mockedStatic.when(() -> OnlinePaymentConfig.hmacSHA512(anyString(), anyString())).thenReturn("mockedHash");

            // Act
            RespMessage result = onlinePaymentService.createVNPayPayment(amount, request);

            // Assert
            assertEquals(Constant.SUCCESS, result.getRespCode());
            assertEquals("Success", result.getRespDesc());
            PaymentResponse responseData = (PaymentResponse) result.getData();
            assertEquals("OK", responseData.getStatus());
            assertTrue(responseData.getURL().contains("vnp_SecureHash=mockedHash"));
            verify(messageBuilder).buildSuccessMessage(any(PaymentResponse.class));
        }
    }

    @Test
    void createVNPayPayment_WithZeroAmount_ShouldThrowException() {
        // Arrange
        int amount = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> onlinePaymentService.createVNPayPayment(amount, request));
        assertEquals("amount must be greater than 0", exception.getMessage());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void createVNPayPayment_WhenHashGenerationFails_ShouldThrowException() {
        // Arrange
        int amount = 1000;
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Mock static method to throw exception
        try (var mockedStatic = mockStatic(OnlinePaymentConfig.class)) {
            // Only stub the method that leads to the exception
            mockedStatic.when(() -> OnlinePaymentConfig.hmacSHA512(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Hash error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> onlinePaymentService.createVNPayPayment(amount, request));
            assertEquals("Error in generating secure hash", exception.getMessage());
            verify(messageBuilder, never()).buildSuccessMessage(any());
        }
    }
    // endregion

    // region handleVNPayReturn
    @Test
    void handleVNPayReturn_WithSuccessResponseCode_ShouldRedirectWithSuccess() {
        // Arrange
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TxnRef")).thenReturn("12345678");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("TXN123");
        when(request.getParameter("vnp_Amount")).thenReturn("100000");
        when(request.getParameter("vnp_PayDate")).thenReturn("20230405123000");

        // Act
        RedirectView result = onlinePaymentService.handleVNPayReturn(request);

        // Assert
        String expectedUrl = "http://frontend.com/order-status?status=success&txnRef=12345678&transactionNo=TXN123&amount=100000&payDate=20230405123000";
        assertEquals(expectedUrl, result.getUrl());
    }

    @Test
    void handleVNPayReturn_WithFailureResponseCode_ShouldRedirectWithFail() {
        // Arrange
        when(request.getParameter("vnp_ResponseCode")).thenReturn("01");

        // Act
        RedirectView result = onlinePaymentService.handleVNPayReturn(request);

        // Assert
        assertEquals("http://frontend.com/order-status?status=fail", result.getUrl());
    }
    // endregion

    // region handleVNPayRefund
    @Test
    void handleVNPayRefund_WithValidOrderId_ShouldProcessRefundSuccessfully() throws Exception {
        // Arrange
        long orderId = 1L;
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(respMessage);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Mock static method in OnlinePaymentConfig
        try (var mockedStatic = mockStatic(OnlinePaymentConfig.class)) {
            mockedStatic.when(() -> OnlinePaymentConfig.getRandomNumber(8)).thenReturn("87654321");
            mockedStatic.when(() -> OnlinePaymentConfig.getIpAddress(request)).thenReturn("127.0.0.1");
            mockedStatic.when(() -> OnlinePaymentConfig.hmacSHA512(anyString(), anyString())).thenReturn("mockedHash");

            // Mock HttpURLConnection directly
            HttpURLConnection mockedConnection = mock(HttpURLConnection.class);
            when(mockedConnection.getResponseCode()).thenReturn(200);
            String responseJson = "{\"vnp_ResponseCode\": \"00\"}";
            InputStream responseStream = new ByteArrayInputStream(responseJson.getBytes());
            when(mockedConnection.getInputStream()).thenReturn(responseStream);

            // Mock URL to return mocked connection
            URL mockedUrl = mock(URL.class);
            when(mockedUrl.openConnection()).thenReturn(mockedConnection);

            // Mock URL creation
            try (var urlMockedStatic = mockStatic(URL.class)) {
                urlMockedStatic.when(() -> new URL(OnlinePaymentConfig.vnp_ApiUrl)).thenReturn(mockedUrl);

                // Act
                RespMessage result = onlinePaymentService.handleVNPayRefund(orderId, request);

                // Assert
                assertEquals(Constant.SUCCESS, result.getRespCode());
                assertEquals(OrderStatus.Cancelled, order.getStatus());
                verify(orderRepository).save(any(Order.class));
                verify(transactionRepository).save(any(Transaction.class));
                verify(messageBuilder).buildSuccessMessage(any());
            }
        }
    }

    @Test
    void handleVNPayRefund_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        long orderId = 1L;
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> onlinePaymentService.handleVNPayRefund(orderId, request));
        assertEquals("Transaction not found", exception.getMessage());
        verify(transactionRepository).findByOrderId(orderId);
        verify(orderRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void handleVNPayRefund_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        long orderId = 1L;
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> onlinePaymentService.handleVNPayRefund(orderId, request));
        assertEquals(Constant.NOT_FOUND, exception.getCode());
        assertEquals("No order found", exception.getMessage());
        verify(transactionRepository).findByOrderId(orderId);
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleVNPayRefund_WhenRefundFails_ShouldThrowException() throws Exception {
        // Arrange
        long orderId = 1L;
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Mock static method in OnlinePaymentConfig
        try (var mockedStatic = mockStatic(OnlinePaymentConfig.class)) {
            mockedStatic.when(() -> OnlinePaymentConfig.getRandomNumber(8)).thenReturn("87654321");
            mockedStatic.when(() -> OnlinePaymentConfig.getIpAddress(request)).thenReturn("127.0.0.1");
            mockedStatic.when(() -> OnlinePaymentConfig.hmacSHA512(anyString(), anyString())).thenReturn("mockedHash");

            // Mock HttpURLConnection directly
            HttpURLConnection mockedConnection = mock(HttpURLConnection.class);
            when(mockedConnection.getResponseCode()).thenReturn(200);
            String responseJson = "{\"vnp_ResponseCode\": \"99\"}";
            InputStream responseStream = new ByteArrayInputStream(responseJson.getBytes());
            when(mockedConnection.getInputStream()).thenReturn(responseStream);

            // Mock URL to return mocked connection
            URL mockedUrl = mock(URL.class);
            when(mockedUrl.openConnection()).thenReturn(mockedConnection);

            // Mock URL creation
            try (var urlMockedStatic = mockStatic(URL.class)) {
                urlMockedStatic.when(() -> new URL(OnlinePaymentConfig.vnp_ApiUrl)).thenReturn(mockedUrl);

                // Act & Assert
                Exception exception = assertThrows(Exception.class,
                        () -> onlinePaymentService.handleVNPayRefund(orderId, request));
                assertEquals("Transaction not found", exception.getMessage());
                verify(orderRepository, never()).save(any());
                verify(transactionRepository, never()).save(any());
            }
        }
    }

    @Test
    void handleVNPayRefund_WhenSaveFails_ShouldThrowException() throws Exception {
        // Arrange
        long orderId = 1L;
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(transaction));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("DB Error"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Mock static method in OnlinePaymentConfig
        try (var mockedStatic = mockStatic(OnlinePaymentConfig.class)) {
            mockedStatic.when(() -> OnlinePaymentConfig.getRandomNumber(8)).thenReturn("87654321");
            mockedStatic.when(() -> OnlinePaymentConfig.getIpAddress(request)).thenReturn("127.0.0.1");
            mockedStatic.when(() -> OnlinePaymentConfig.hmacSHA512(anyString(), anyString())).thenReturn("mockedHash");

            // Mock HttpURLConnection directly
            HttpURLConnection mockedConnection = mock(HttpURLConnection.class);
            when(mockedConnection.getResponseCode()).thenReturn(200);
            String responseJson = "{\"vnp_ResponseCode\": \"00\"}";
            InputStream responseStream = new ByteArrayInputStream(responseJson.getBytes());
            when(mockedConnection.getInputStream()).thenReturn(responseStream);

            // Mock URL to return mocked connection
            URL mockedUrl = mock(URL.class);
            when(mockedUrl.openConnection()).thenReturn(mockedConnection);

            // Mock URL creation
            try (var urlMockedStatic = mockStatic(URL.class)) {
                urlMockedStatic.when(() -> new URL(OnlinePaymentConfig.vnp_ApiUrl)).thenReturn(mockedUrl);

                // Act & Assert
                CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                        () -> onlinePaymentService.handleVNPayRefund(orderId, request));
                assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
                assertEquals("Cannot save transaction", exception.getMessage());
                verify(orderRepository).save(any(Order.class));
                verify(transactionRepository, never()).save(any());
            }
        }
    }
    // endregion

    // Helper method to set private fields for testing
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}