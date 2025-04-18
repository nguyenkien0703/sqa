package com.ptit.coffee_shop.security;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.payload.response.RespMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho lớp CustomAuthenticationEntryPoint
 */
class CustomAuthenticationEntryPointTest {

    private MessageBuilder messageBuilder;
    private CustomAuthenticationEntryPoint entryPoint;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationException exception;

    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws Exception {
        messageBuilder = mock(MessageBuilder.class);
        entryPoint = new CustomAuthenticationEntryPoint(messageBuilder);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        exception = new BadCredentialsException("Invalid credentials");

        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    /**
     * ✅ TC001
     * ✅ Hàm được test: commence()
     * ✅ Mô tả: Nếu request có attribute "exception", hàm sẽ sử dụng chính RespMessage này để trả về cho client.
     * ✅ Input: request.setAttribute("exception", RespMessage)
     * ✅ Expected output:
     *      - HTTP status = 401
     *      - Content-Type = application/json
     *      - Nội dung JSON chứa respCode và respDesc từ attribute
     */
    @Test
    void commence_WhenRequestHasExceptionAttribute_UsesThatMessage() throws Exception {
        // Arrange
        RespMessage existingResp = RespMessage.builder()
                .respCode("EXISTING_CODE")
                .respDesc("Đã có message sẵn trong request")
                .data(null)
                .build();

        when(request.getAttribute("exception")).thenReturn(existingResp);

        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");

        String output = stringWriter.toString();
        assertTrue(output.contains("EXISTING_CODE"));
        assertTrue(output.contains("Đã có message sẵn trong request"));
    }

    /**
     * ✅ TC002
     * ✅ Hàm được test: commence()
     * ✅ Mô tả: Nếu request KHÔNG có attribute "exception", thì messageBuilder sẽ được dùng để tạo ra RespMessage trả về.
     * ✅ Input: request không có attribute, messageBuilder.buildFailureMessage trả về message với code UNAUTHORIZED
     * ✅ Expected output:
     *      - HTTP status = 401
     *      - Content-Type = application/json
     *      - Nội dung JSON chứa respCode = "UNAUTHORIZED" và chuỗi lỗi trong respDesc
     */
    @Test
    void commence_WhenNoExceptionAttribute_UsesMessageBuilder() throws Exception {
        // Arrange
        when(request.getAttribute("exception")).thenReturn(null);

        RespMessage builtResp = RespMessage.builder()
                .respCode(Constant.UNAUTHORIZED)
                .respDesc("Authentication error: " + exception)
                .data(null)
                .build();

        when(messageBuilder.buildFailureMessage(
                eq(Constant.UNAUTHORIZED),
                isNull(),
                ArgumentMatchers.contains("Authentication error")
        )).thenReturn(builtResp);

        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        verify(messageBuilder).buildFailureMessage(eq(Constant.UNAUTHORIZED), isNull(), anyString());
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");

        String output = stringWriter.toString();
        assertTrue(output.contains(Constant.UNAUTHORIZED));
        assertTrue(output.contains("Authentication error"));
    }
}
