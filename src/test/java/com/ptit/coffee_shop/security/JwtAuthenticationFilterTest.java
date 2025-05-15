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
     * TC1: Test với token hợp lệ.
     * Input: Token hợp lệ trong header request.
     * Mục tiêu: Kiểm tra xem khi token hợp lệ thì thông tin người dùng được set vào SecurityContextHolder.
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
     * TC2: Test với token không hợp lệ.
     * Input: Token không hợp lệ trong header request.
     * Mục tiêu: Kiểm tra xem khi token không hợp lệ thì không có gì được set vào SecurityContextHolder.
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
     * TC3: Test khi token ném ra CoffeeShopException.
     * Input: Token gây ra CoffeeShopException khi validate.
     * Mục tiêu: Kiểm tra xem khi token ném ra `CoffeeShopException`, thông báo lỗi được gán vào request attribute.
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
     * TC4: Test khi validateToken ném ra exception khác.
     * Input: Token gây ra RuntimeException khi validate.
     * Mục tiêu: Kiểm tra xem khi token ném ra exception khác, thông báo lỗi với mã UNDEFINED được gán vào request attribute.
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

    /**
     * TC5: Test khi không có authorization header.
     * Input: Request không có authorization header.
     * Mục tiêu: Kiểm tra xem khi không có header, không có gì được set vào SecurityContextHolder.
     */
    @Test
    @DisplayName("TC5 - Không có authorization header: không set gì vào SecurityContextHolder")
    void test_TC5_noAuthorizationHeader_doesNotSetAuthentication() throws Exception {
        // Given: Request không có authorization header
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When: Gọi phương thức doFilterInternal với request không có header
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra không có gì được set vào SecurityContextHolder
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        // Verify validateToken không được gọi vì không có token
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    /**
     * TC6: Test khi authorization header không bắt đầu bằng "Bearer ".
     * Input: Authorization header không đúng định dạng.
     * Mục tiêu: Kiểm tra xem khi header không đúng định dạng, không có gì được set vào SecurityContextHolder.
     */
    @Test
    @DisplayName("TC6 - Authorization header không đúng định dạng: không set gì vào SecurityContextHolder")
    void test_TC6_invalidAuthorizationHeaderFormat_doesNotSetAuthentication() throws Exception {
        // Given: Header không bắt đầu bằng "Bearer "
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Token some.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When: Gọi phương thức doFilterInternal với request có header không đúng định dạng
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra không có gì được set vào SecurityContextHolder
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        // Verify validateToken không được gọi vì token không được trích xuất
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    /**
     * TC7: Test khi UserDetailsService ném ra exception.
     * Input: Token hợp lệ nhưng UserDetailsService ném ra exception.
     * Mục tiêu: Kiểm tra xem khi UserDetailsService ném ra exception, thông báo lỗi được gán vào request attribute.
     */
    @Test
    @DisplayName("TC7 - UserDetailsService ném exception: gán lỗi UNDEFINED vào request")
    void test_TC7_userDetailsServiceThrowsException_setsUndefinedExceptionAttribute() throws Exception {
        // Given: Token hợp lệ nhưng UserDetailsService ném exception
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer valid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken("valid.token.here")).thenReturn(true);
        when(jwtTokenProvider.getUsername("valid.token.here")).thenReturn("testuser");
        
        // UserDetailsService ném exception
        when(userDetailsService.loadUserByUsername("testuser"))
                .thenThrow(new RuntimeException("User not found"));
        
        // RespMessage với mã UNDEFINED
        RespMessage mockResp = new RespMessage(Constant.UNDEFINED, "User not found", null);
        when(messageBuilder.buildFailureMessage(Constant.UNDEFINED, null, "User not found")).thenReturn(mockResp);

        // When: Gọi phương thức doFilterInternal với request gây lỗi
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra attribute exception trên request chứa thông báo lỗi
        assertEquals(mockResp, request.getAttribute("exception"));
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * TC8: Test với token rỗng.
     * Input: Authorization header có format "Bearer " nhưng token rỗng.
     * Mục tiêu: Kiểm tra xem khi token rỗng, không có gì được set vào SecurityContextHolder.
     */
    @Test
    @DisplayName("TC8 - Token rỗng: không set gì vào SecurityContextHolder")
    void test_TC8_emptyToken_doesNotSetAuthentication() throws Exception {
        // Given: Token rỗng (chỉ có "Bearer ")
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When: Gọi phương thức doFilterInternal với request có token rỗng
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then: Kiểm tra không có gì được set vào SecurityContextHolder
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        // Verify validateToken không được gọi với token rỗng
        verify(jwtTokenProvider, never()).validateToken("");
    }
}
