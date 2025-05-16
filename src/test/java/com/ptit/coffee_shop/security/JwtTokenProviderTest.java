package com.ptit.coffee_shop.security;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.response.LoginResponse;
import com.ptit.coffee_shop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    private User testUser;
    private final String TEST_JWT_SECRET = "28DB7E8FCCB6E7491D4D4765A03CB2A814C7237A74B521628FF00578430BA516";
    private final long TEST_JWT_EXPIRATION = 900000; // 15 minutes
    private final long TEST_REFRESH_EXPIRATION = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationDate", TEST_JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationDate", TEST_REFRESH_EXPIRATION);

        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setStatus(Status.ACTIVE);
        testUser.setRole(role);
    }

    @Test
    //Tạo JWT token cho người dùng hợp lệ
    public void whenGenerateToken_thenReturnLoginResponse() {
        
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        LoginResponse loginResponse = jwtTokenProvider.generateToken(authentication);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getRefreshToken()).isNotNull();
        assertThat(loginResponse.getExpiresIn()).isEqualTo((int) TEST_JWT_EXPIRATION);
        assertThat(loginResponse.getRefreshExpiresIn()).isEqualTo((int) TEST_REFRESH_EXPIRATION);
    }

    @Test
    //Xử lý trường hợp tạo token với user không tồn tại
    public void whenGenerateToken_withInvalidUser_thenThrowException() {
        
        when(authentication.getName()).thenReturn("invalid@example.com");
        when(userService.getUserByEmail("invalid@example.com")).thenReturn(Optional.empty());

        
        assertThrows(CoffeeShopException.class, () -> jwtTokenProvider.generateToken(authentication));
    }

    @Test
    //Xác thực tính hợp lệ của token
    public void whenValidateToken_withValidToken_thenReturnTrue() {
        
        String token = jwtTokenProvider.generateAccessToken("test@example.com");

        
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    //Xử lý token không hợp lệ
    public void whenValidateToken_withInvalidToken_thenThrowException() {
        
        assertThrows(CoffeeShopException.class, () -> jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    //Trích xuất username từ token
    public void whenGetUsername_thenReturnCorrectUsername() {
        
        String username = "test@example.com";
        String token = jwtTokenProvider.generateAccessToken(username);

        String extractedUsername = jwtTokenProvider.getUsername(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    //Tạo và so sánh access token và refresh token
    public void whenGenerateAccessAndRefreshTokens_thenTokensAreDifferent() {
        
        String username = "test@example.com";

        String accessToken = jwtTokenProvider.generateAccessToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }




} 