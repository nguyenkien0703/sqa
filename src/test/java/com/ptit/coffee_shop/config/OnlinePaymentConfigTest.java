package com.ptit.coffee_shop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OnlinePaymentConfigTest {

    /**
     * TC1: Test getIpAddress khi có header X-FORWARDED-FOR.
     * Kết quả mong đợi: Trả về IP từ header X-FORWARDED-FOR.
     */
    @Test
    void TC1_getIpAddress_withXForwardedFor() {
        // Given: Mock HttpServletRequest với header X-FORWARDED-FOR có giá trị.
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-FORWARDED-FOR")).thenReturn("192.168.1.100");

        // When: Gọi phương thức getIpAddress
        String ip = OnlinePaymentConfig.getIpAddress(mockRequest);

        // Then: Kiểm tra địa chỉ IP trả về là giá trị trong header X-FORWARDED-FOR
        assertEquals("192.168.1.100", ip);
    }

    /**
     * TC2: Test getIpAddress khi không có header X-FORWARDED-FOR.
     * Kết quả mong đợi: Trả về địa chỉ IP từ getRemoteAddr().
     */
    @Test
    void TC2_test_getIpAddress_withoutXForwardedFor() {
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
     * TC3: Test getIpAddress khi có lỗi xảy ra trong quá trình lấy IP.
     * Kết quả mong đợi: Trả về chuỗi lỗi "Invalid IP: [error message]".
     */
    @Test
    void TC3_test_getIpAddress_withException() {
        // Given: Mock HttpServletRequest gây ra ngoại lệ khi lấy IP.
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader("X-FORWARDED-FOR")).thenThrow(new RuntimeException("Test exception"));

        // When: Gọi phương thức getIpAddress
        String ip = OnlinePaymentConfig.getIpAddress(mockRequest);

        // Then: Kiểm tra IP trả về là thông báo lỗi với ngoại lệ.
        assertEquals("Invalid IP:Test exception", ip);
    }

    /**
     * TC04: Test getRandomNumber với chiều dài len = 6.
     * Kết quả mong đợi: Chuỗi trả về có độ dài là 6 và chỉ chứa các chữ số.
     */
    @Test
    void TC04_test_getRandomNumber() {
        // Given: Chiều dài chuỗi mong muốn là 6.
        int len = 6;

        // When: Gọi phương thức getRandomNumber
        String result = OnlinePaymentConfig.getRandomNumber(len);

        // Then: Kiểm tra độ dài của chuỗi trả về và các ký tự trong chuỗi.
        assertEquals(len, result.length());  // Kiểm tra độ dài chuỗi
        result.chars().forEach(c -> assertTrue(Character.isDigit(c)));  // Kiểm tra các ký tự có phải là chữ số không.
    }

    /**
     * TC05: Test getRandomNumber với chiều dài len = 10.
     * Kết quả mong đợi: Chuỗi trả về có độ dài là 10 và chỉ chứa các chữ số.
     */
    @Test
    void TC05_test_getRandomNumber_length10() {
        // Given: Chiều dài chuỗi mong muốn là 10.
        int len = 10;

        // When: Gọi phương thức getRandomNumber
        String result = OnlinePaymentConfig.getRandomNumber(len);

        // Then: Kiểm tra độ dài của chuỗi trả về và các ký tự trong chuỗi.
        assertEquals(len, result.length());  // Kiểm tra độ dài chuỗi
        result.chars().forEach(c -> assertTrue(Character.isDigit(c)));  // Kiểm tra các ký tự có phải là chữ số không.
    }

    /**
     * TC06: Test hmacSHA512 với key và data hợp lệ.
     * Kết quả mong đợi: Trả về một chuỗi mã hóa không rỗng.
     */
    @Test
    void TC06_test_hmacSHA512_validInput() {
        // Given: Key và data hợp lệ
        String key = "secretKey";
        String data = "dataToEncrypt";

        // When: Gọi phương thức hmacSHA512
        String result = OnlinePaymentConfig.hmacSHA512(key, data);

        // Then: Kiểm tra kết quả trả về không rỗng và có độ dài đúng (SHA512 tạo ra 128 ký tự hex)
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(128, result.length());
        
        // Đảm bảo kết quả chỉ chứa các ký tự hex (0-9, a-f)
        assertTrue(result.matches("^[0-9a-f]+$"));
    }

    /**
     * TC07: Test hmacSHA512 với cùng key và data luôn cho kết quả giống nhau.
     * Kết quả mong đợi: Hai lần gọi với cùng đầu vào sẽ cho kết quả giống nhau.
     */
    @Test
    void TC07_test_hmacSHA512_consistency() {
        // Given: Key và data hợp lệ
        String key = "testKey123";
        String data = "testData456";

        // When: Gọi phương thức hmacSHA512 hai lần với cùng đầu vào
        String result1 = OnlinePaymentConfig.hmacSHA512(key, data);
        String result2 = OnlinePaymentConfig.hmacSHA512(key, data);

        // Then: Kiểm tra kết quả của hai lần gọi là giống nhau
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
    }

    /**
     * TC08: Test hmacSHA512 với key là null.
     * Kết quả mong đợi: Trả về chuỗi rỗng do xử lý ngoại lệ.
     */
    @Test
    void TC08_test_hmacSHA512_nullKey() {
        // Given: Key là null, data hợp lệ
        String key = null;
        String data = "testData";

        // When: Gọi phương thức hmacSHA512
        String result = OnlinePaymentConfig.hmacSHA512(key, data);

        // Then: Kiểm tra kết quả trả về là chuỗi rỗng
        assertEquals("", result);
    }

    /**
     * TC09: Test hmacSHA512 với data là null.
     * Kết quả mong đợi: Trả về chuỗi rỗng do xử lý ngoại lệ.
     */
    @Test
    void TC09_test_hmacSHA512_nullData() {
        // Given: Key hợp lệ, data là null
        String key = "testKey";
        String data = null;

        // When: Gọi phương thức hmacSHA512
        String result = OnlinePaymentConfig.hmacSHA512(key, data);

        // Then: Kiểm tra kết quả trả về là chuỗi rỗng
        assertEquals("", result);
    }

    /**
     * TC10: Test hmacSHA512 với key và data đều là null.
     * Kết quả mong đợi: Trả về chuỗi rỗng do xử lý ngoại lệ.
     */
    @Test
    void TC10_test_hmacSHA512_bothNull() {
        // Given: Key và data đều là null
        String key = null;
        String data = null;

        // When: Gọi phương thức hmacSHA512
        String result = OnlinePaymentConfig.hmacSHA512(key, data);

        // Then: Kiểm tra kết quả trả về là chuỗi rỗng
        assertEquals("", result);
    }

}
