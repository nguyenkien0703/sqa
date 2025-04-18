package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.ChangePasswordDTO;
import com.ptit.coffee_shop.payload.request.LoginRequest;
import com.ptit.coffee_shop.payload.request.RegisterRequest;
import com.ptit.coffee_shop.payload.response.LoginResponse;
import com.ptit.coffee_shop.repository.RoleRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import com.ptit.coffee_shop.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private Role userRole;
    private User testUser;

    @BeforeEach
    public void setUp() {
        userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(0, RoleEnum.ROLE_USER)));

        testUser = User.builder()
                .email("testuser@example.com")
                .password(passwordEncoder.encode("password"))
                .role(userRole)
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("Test User")
                .phone("123456789")
                .profile_img("profile_img_url")
                .build();

        userRepository.save(testUser);
    }

    // ============ LOGIN TESTS ============

    /**
     * TC001
     * Function: login
     * Description: Đăng nhập thành công với email và mật khẩu hợp lệ.
     * Input: email = "testuser@example.com", password = "password"
     * Expected Output: Không throw exception, responseCode = "000"
     */
    @Test
    public void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        assertDoesNotThrow(() -> {
            var response = authService.login(loginRequest);
            assertNotNull(response);
            assertEquals("000", response.getRespCode());
        });
    }

    /**
     * TC002
     * Function: login
     * Description: Sai mật khẩu.
     * Input: email = "testuser@example.com", password = "wrongpassword"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "wrongpassword");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC003
     * Function: login
     * Description: Email không tồn tại trong hệ thống.
     * Input: email = "nonexistentuser@example.com", password = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginUserNotFound() {
        LoginRequest loginRequest = new LoginRequest("nonexistentuser@example.com", "password");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC004
     * Function: login
     * Description: Tài khoản bị vô hiệu hóa (INACTIVE).
     * Input: email = "testuser@example.com", password = "password", status = INACTIVE
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginUserDisabled() {
        testUser.setStatus(Status.INACTIVE);
        userRepository.save(testUser);
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC005
     * Function: login
     * Description: Thiếu email trong request.
     * Input: email = null, password = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginMissingEmail() {
        LoginRequest loginRequest = new LoginRequest(null, "password");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC006
     * Function: login
     * Description: Email rỗng trong request.
     * Input: email = "", password = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginEmailEmpty() {
        LoginRequest loginRequest = new LoginRequest("", "password");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC007
     * Function: login
     * Description: Mật khẩu rỗng.
     * Input: email = "testuser@example.com", password = ""
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginPasswordEmpty() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "");
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

    /**
     * TC008
     * Function: login
     * Description: Thiếu mật khẩu.
     * Input: email = "testuser@example.com", password = null
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testLoginMissingPassword() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", null);
        assertThrows(CoffeeShopException.class, () -> authService.login(loginRequest));
    }

// ============ REGISTER TESTS ============

    /**
     * TC009
     * Function: register
     * Description: Đăng ký thành công với dữ liệu hợp lệ.
     * Input: email = "newuser@example.com", password = "password", confirmPassword = "password"
     * Expected Output: Không throw exception, responseCode = "000"
     */
    @Test
    public void testRegisterSuccess() {
        RegisterRequest registerRequest = new RegisterRequest("newuser@example.com", "password", "password");
        assertDoesNotThrow(() -> {
            var response = authService.register(registerRequest);
            assertNotNull(response);
            assertEquals("000", response.getRespCode());
        });
    }

    /**
     * TC010
     * Function: register
     * Description: Email đã tồn tại trong hệ thống.
     * Input: email = "testuser@example.com", password = "password", confirmPassword = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterEmailExists() {
        RegisterRequest registerRequest = new RegisterRequest("testuser@example.com", "password", "password");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC011
     * Function: register
     * Description: Mật khẩu và xác nhận mật khẩu không khớp.
     * Input: email = "newuser2@example.com", password = "password", confirmPassword = "mismatch"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterPasswordMismatch() {
        RegisterRequest registerRequest = new RegisterRequest("newuser2@example.com", "password", "mismatch");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC012
     * Function: register
     * Description: Thiếu email.
     * Input: email = null, password = "password", confirmPassword = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterMissingEmail() {
        RegisterRequest registerRequest = new RegisterRequest(null, "password", "password");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC013
     * Function: register
     * Description: Thiếu mật khẩu.
     * Input: email = "newuser3@example.com", password = null, confirmPassword = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterMissingPassword() {
        RegisterRequest registerRequest = new RegisterRequest("newuser3@example.com", null, "password");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC014
     * Function: register
     * Description: Thiếu xác nhận mật khẩu.
     * Input: email = "anotheruser@example.com", password = "password", confirmPassword = null
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterConfirmPasswordMissing() {
        RegisterRequest registerRequest = new RegisterRequest("anotheruser@example.com", "password", null);
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC015
     * Function: register
     * Description: Email rỗng.
     * Input: email = "", password = "password", confirmPassword = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterEmailEmpty() {
        RegisterRequest registerRequest = new RegisterRequest("", "password", "password");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC016
     * Function: register
     * Description: Mật khẩu rỗng.
     * Input: email = "newuser@example.com", password = "", confirmPassword = "password"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterPasswordEmpty() {
        RegisterRequest registerRequest = new RegisterRequest("newuser@example.com", "", "password");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }

    /**
     * TC017
     * Function: register
     * Description: Xác nhận mật khẩu rỗng.
     * Input: email = "newuser@example.com", password = "password", confirmPassword = ""
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRegisterConfirmPasswordEmpty() {
        RegisterRequest registerRequest = new RegisterRequest("newuser@example.com", "password", "");
        assertThrows(CoffeeShopException.class, () -> authService.register(registerRequest));
    }


// ============ CHANGE PASSWORD TESTS ============

    /**
     * TC018
     * Function: changePassword
     * Description: Đổi mật khẩu thành công.
     * Input: oldPassword = "password", newPassword = "newpassword", confirmPassword = "newpassword"
     * Expected Output: Không throw exception, responseCode = "000", mật khẩu được cập nhật
     */
    @Test
    public void testChangePasswordSuccess() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", "newpassword", "newpassword");
        assertDoesNotThrow(() -> {
            var response = authService.changePassword(dto);
            assertNotNull(response);
            assertEquals("000", response.getRespCode());
        });

        User updated = userRepository.findByEmail("testuser@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("newpassword", updated.getPassword()));
    }

    /**
     * TC019
     * Function: changePassword
     * Description: Mật khẩu cũ không chính xác.
     * Input: oldPassword = "wrongpassword", newPassword = "newpassword", confirmPassword = "newpassword"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordOldPasswordIncorrect() {
        ChangePasswordDTO dto = new ChangePasswordDTO("wrongpassword", "newpassword", "newpassword");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC020
     * Function: changePassword
     * Description: Mật khẩu mới và xác nhận không khớp.
     * Input: oldPassword = "password", newPassword = "newpassword", confirmPassword = "mismatch"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordNewPasswordMismatch() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", "newpassword", "mismatch");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC021
     * Function: changePassword
     * Description: Mật khẩu mới null.
     * Input: oldPassword = "password", newPassword = null, confirmPassword = "mismatch"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordNewPasswordNull() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", null, "mismatch");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC022
     * Function: changePassword
     * Description: Mật khẩu cũ null.
     * Input: oldPassword = null, newPassword = "newpassword", confirmPassword = "mismatch"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordOldPasswordNull() {
        ChangePasswordDTO dto = new ChangePasswordDTO(null, "newpassword", "mismatch");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC023
     * Function: changePassword
     * Description: Xác nhận mật khẩu null.
     * Input: oldPassword = "password", newPassword = "newpassword", confirmPassword = null
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordConfirmPasswordNull() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", "newpassword", null);
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC024
     * Function: changePassword
     * Description: Mật khẩu mới rỗng.
     * Input: oldPassword = "password", newPassword = "", confirmPassword = "newpassword"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordNewPasswordEmpty() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", "", "newpassword");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC025
     * Function: changePassword
     * Description: Mật khẩu cũ rỗng.
     * Input: oldPassword = "", newPassword = "newpassword", confirmPassword = "newpassword"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordOldPasswordEmpty() {
        ChangePasswordDTO dto = new ChangePasswordDTO("", "newpassword", "newpassword");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

    /**
     * TC026
     * Function: changePassword
     * Description: Xác nhận mật khẩu rỗng.
     * Input: oldPassword = "password", newPassword = "newpassword", confirmPassword = ""
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testChangePasswordConfirmPasswordEmpty() {
        ChangePasswordDTO dto = new ChangePasswordDTO("password", "newpassword", "");
        assertThrows(CoffeeShopException.class, () -> authService.changePassword(dto));
    }

// ============ PROFILE TESTS ============

    /**
     * TC027
     * Function: getProfileByToken
     * Description: Lấy profile bằng token thành công sau khi login.
     * Input: Login với email = "testuser@example.com", password = "password"
     * Expected Output: Không throw exception, responseCode = "000"
     */
    @Test
    public void testGetProfileByTokenSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        authService.login(loginRequest);
        assertDoesNotThrow(() -> {
            var response = authService.getProfileByToken();
            assertNotNull(response);
            assertEquals("000", response.getRespCode());
        });
    }



// ============ REFRESH TOKEN TESTS ============

    /**
     * TC029
     * Function: refreshAccessToken
     * Description: Refresh token hợp lệ, trả về token mới.
     * Input: Refresh token từ login
     * Expected Output: Không throw exception, responseCode = "000"
     */
    @Test
    public void testRefreshAccessTokenSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        var loginResponse = authService.login(loginRequest);
        LoginResponse loginData = (LoginResponse) loginResponse.getData();
        String refreshToken = loginData.getRefreshToken();

        assertDoesNotThrow(() -> {
            var response = authService.refreshAccessToken("Bearer " + refreshToken);
            assertNotNull(response);
            assertEquals("000", response.getRespCode());
        });
    }

    /**
     * TC030
     * Function: refreshAccessToken
     * Description: Token không hợp lệ.
     * Input: "Bearer invalidToken"
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRefreshAccessTokenInvalid() {
        // Arrange
        String invalidToken = "";  // Hoặc token giả có format không hợp lệ như "Bearer invalidToken"

        // Mock jwtTokenProvider.validateToken trả về false
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(false);

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            authService.refreshAccessToken(invalidToken); // Gọi hàm refreshAccessToken với token invalid
        });

        // Kiểm tra thông báo lỗi
        assertEquals("Invalid refresh token", exception.getMessage());
    }



    /**
     * TC031
     * Function: refreshAccessToken
     * Description: Thiếu prefix "Bearer ".
     * Input: chỉ có token, không có tiền tố "Bearer "
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRefreshAccessTokenMissingBearerPrefix() {
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");
        var loginResponse = authService.login(loginRequest);
        LoginResponse loginData = (LoginResponse) loginResponse.getData();
        String refreshToken = loginData.getRefreshToken();
        assertThrows(CoffeeShopException.class, () -> authService.refreshAccessToken(refreshToken));
    }

    /**
     * TC032
     * Function: refreshAccessToken
     * Description: Token null.
     * Input: refreshToken = null
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testRefreshAccessTokenNullToken() {
        String refreshToken = null;
        assertThrows(CoffeeShopException.class, () -> authService.refreshAccessToken(refreshToken));
    }

    /**
     * TC033
     * Function: refreshAccessToken
     * Description: Token không hợp lệ (trường hợp validateToken trả về false).
     * Input: refreshToken = "invalidToken"
     * Expected Output: Throw CoffeeShopException với thông báo "Invalid refresh token"
     */
    @Test
    public void testRefreshAccessTokenInvalidToken() {
        String refreshToken = "invalidToken";
        assertThrows(CoffeeShopException.class, () -> {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new CoffeeShopException(Constant.FIELD_NOT_VALID, new Object[]{"RefreshToken"}, "Invalid refresh token");
            }
        });
    }
    /**
     * TC028
     * Function: getProfileByToken
     * Description: Không có token khi gọi getProfileByToken.
     * Input: Không login
     * Expected Output: Throw CoffeeShopException
     */
    @Test
    public void testGetProfileByTokenNoToken() {
        SecurityContextHolder.clearContext();
        assertThrows(CoffeeShopException.class, () -> authService.getProfileByToken());
    }
}
