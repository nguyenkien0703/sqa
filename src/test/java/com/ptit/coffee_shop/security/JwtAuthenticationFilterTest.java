package com.ptit.coffee_shop.security;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.payload.response.RespMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, messageBuilder);
    }

    /**
     * ✅ TC1: Test với token hợp lệ.
     * ➤ Input: Token hợp lệ trong header request.
     * ➤ Mục tiêu: Kiểm tra xem khi token hợp lệ thì thông tin người dùng được set vào SecurityContextHolder.
     */
    @Test
    @DisplayName("TC1 - Token hợp lệ: xác thực thành công và set vào SecurityContextHolder")
    void test_TC1_validToken_setsAuthentication() throws Exception {
        // Given: Token hợp lệ và thông tin người dùng
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken("valid.token.here")).thenReturn(true);
        when(jwtTokenProvider.getUsername("valid.token.here")).thenReturn("testuser");
        User mockUser = new User("testuser", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUser);

        // When: Gọi phương thức doFilterInternal với request có token hợp lệ
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra thông tin người dùng đã được set vào SecurityContextHolder
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * ✅ TC2: Test với token không hợp lệ.
     * ➤ Input: Token không hợp lệ trong header request.
     * ➤ Mục tiêu: Kiểm tra xem khi token không hợp lệ thì không có gì được set vào SecurityContextHolder.
     */
    @Test
    @DisplayName("TC2 - Token không hợp lệ: không set gì vào SecurityContextHolder")
    void test_TC2_invalidToken_doesNotSetAuthentication() throws Exception {
        // Given: Token không hợp lệ
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

        // When: Gọi phương thức doFilterInternal với request có token không hợp lệ
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra không có gì được set vào SecurityContextHolder
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * ✅ TC3: Test khi token ném ra CoffeeShopException.
     * ➤ Input: Token gây ra CoffeeShopException khi validate.
     * ➤ Mục tiêu: Kiểm tra xem khi token ném ra `CoffeeShopException`, thông báo lỗi được gán vào request attribute.
     */
    @Test
    @DisplayName("TC3 - Token ném CoffeeShopException: gán message lỗi vào request")
    void test_TC3_throwCoffeeShopException_setsExceptionAttribute() throws Exception {
        // Given: Token gây ra CoffeeShopException
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer error.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // CoffeeShopException với code "E100" và thông điệp lỗi "Custom error"
        when(jwtTokenProvider.validateToken("error.token"))
                .thenThrow(new CoffeeShopException("E100", null, "Custom error"));

        // RespMessage được trả về từ MessageBuilder
        RespMessage fakeResp = new RespMessage("E100", "Custom error", null);
        when(messageBuilder.buildFailureMessage("E100", null, "Custom error")).thenReturn(fakeResp);

        // When: Gọi phương thức doFilterInternal với request gây lỗi
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra attribute exception trên request chứa thông báo lỗi
        assertEquals(fakeResp, request.getAttribute("exception"));
        verify(filterChain).doFilter(request, response);
    }

    /**
     * ✅ TC4: Test khi validateToken ném ra exception khác.
     * ➤ Input: Token gây ra RuntimeException khi validate.
     * ➤ Mục tiêu: Kiểm tra xem khi token ném ra exception khác, thông báo lỗi với mã UNDEFINED được gán vào request attribute.
     */
    @Test
    @DisplayName("TC4 - Token ném RuntimeException: gán lỗi UNDEFINED vào request")
    void test_TC4_throwGenericException_setsUndefinedExceptionAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer throw.exception");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Ném ra exception chung RuntimeException
        when(jwtTokenProvider.validateToken("throw.exception"))
                .thenThrow(new RuntimeException("Something went wrong"));

        // RespMessage với mã UNDEFINED
        RespMessage mockResp = new RespMessage(Constant.UNDEFINED, "Something went wrong", null);
        when(messageBuilder.buildFailureMessage(Constant.UNDEFINED, null, "Something went wrong")).thenReturn(mockResp);

        // When: Gọi phương thức doFilterInternal với request gây lỗi
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra attribute exception trên request chứa thông báo lỗi
        assertEquals(mockResp, request.getAttribute("exception"));
        verify(filterChain).doFilter(request, response);
    }
}
