package com.ptit.coffee_shop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnlinePaymentConfigTest {

    /**
     * ✅ TC1: Test getIpAddress khi có header X-FORWARDED-FOR.
     * ➤ Kết quả mong đợi: Trả về IP từ header X-FORWARDED-FOR.
     */
    @Test
    void test_getIpAddress_withXForwardedFor() {
        // Given: Mock HttpServletRequest với header X-FORWARDED-FOR có giá trị.
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-FORWARDED-FOR")).thenReturn("192.168.1.100");

        // When: Gọi phương thức getIpAddress
        String ip = OnlinePaymentConfig.getIpAddress(mockRequest);

        // Then: Kiểm tra địa chỉ IP trả về là giá trị trong header X-FORWARDED-FOR
        assertEquals("192.168.1.100", ip);
    }

    /**
     * ✅ TC2: Test getIpAddress khi không có header X-FORWARDED-FOR.
     * ➤ Kết quả mong đợi: Trả về địa chỉ IP từ getRemoteAddr().
     */
    @Test
    void test_getIpAddress_withoutXForwardedFor() {
        // Given: Mock HttpServletRequest khi không có header X-FORWARDED-FOR, và lấy IP từ getRemoteAddr().
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-FORWARDED-FOR")).thenReturn(null);  // Không có header X-FORWARDED-FOR
        Mockito.when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // When: Gọi phương thức getIpAddress
        String ip = OnlinePaymentConfig.getIpAddress(mockRequest);

        // Then: Kiểm tra IP trả về là "127.0.0.1"
        assertEquals("127.0.0.1", ip);
    }

    /**
     * ✅ TC3: Test getIpAddress khi có lỗi xảy ra trong quá trình lấy IP.
     * ➤ Kết quả mong đợi: Trả về chuỗi lỗi "Invalid IP: [error message]".
     */
    @Test
    void test_getIpAddress_withException() {
        // Given: Mock HttpServletRequest gây ra ngoại lệ khi lấy IP.
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-FORWARDED-FOR")).thenThrow(new RuntimeException("Test exception"));

        // When: Gọi phương thức getIpAddress
        String ip = OnlinePaymentConfig.getIpAddress(mockRequest);

        // Then: Kiểm tra IP trả về là thông báo lỗi với ngoại lệ.
        assertEquals("Invalid IP:Test exception", ip);
    }

    /**
     * ✅ TC1: Test getRandomNumber với chiều dài len = 6.
     * ➤ Kết quả mong đợi: Chuỗi trả về có độ dài là 6 và chỉ chứa các chữ số.
     */
    @Test
    void test_getRandomNumber() {
        // Given: Chiều dài chuỗi mong muốn là 6.
        int len = 6;

        // When: Gọi phương thức getRandomNumber
        String result = OnlinePaymentConfig.getRandomNumber(len);

        // Then: Kiểm tra độ dài của chuỗi trả về và các ký tự trong chuỗi.
        assertEquals(len, result.length());  // Kiểm tra độ dài chuỗi
        result.chars().forEach(c -> assertTrue(Character.isDigit(c)));  // Kiểm tra các ký tự có phải là chữ số không.
    }

    /**
     * ✅ TC2: Test getRandomNumber với chiều dài len = 10.
     * ➤ Kết quả mong đợi: Chuỗi trả về có độ dài là 10 và chỉ chứa các chữ số.
     */
    @Test
    void test_getRandomNumber_length10() {
        // Given: Chiều dài chuỗi mong muốn là 10.
        int len = 10;

        // When: Gọi phương thức getRandomNumber
        String result = OnlinePaymentConfig.getRandomNumber(len);

        // Then: Kiểm tra độ dài của chuỗi trả về và các ký tự trong chuỗi.
        assertEquals(len, result.length());  // Kiểm tra độ dài chuỗi
        result.chars().forEach(c -> assertTrue(Character.isDigit(c)));  // Kiểm tra các ký tự có phải là chữ số không.
    }
}
