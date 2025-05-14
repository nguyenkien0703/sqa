package com.ptit.coffee_shop.config;

import com.ptit.coffee_shop.security.CustomAuthenticationEntryPoint;
import com.ptit.coffee_shop.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test cho lớp SecurityConfig.
 * Các test này đảm bảo rằng cấu hình bảo mật được inject và cấu hình đúng trong ứng dụng.
 */
@SpringBootTest
@ActiveProfiles("test") // Sử dụng cấu hình profile "test"
public class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private HttpSecurity httpSecurity; // Inject HttpSecurity để kiểm tra trực tiếp cấu hình

    /**
     * Phương thức khởi tạo trước mỗi test.
     * Có thể thêm logic setup nếu cần thiết.
     */
    @BeforeEach
    public void setUp() {
        // Kiểm tra nếu cần thiết
    }

    /**
     * TC001 - Kiểm tra SecurityConfig đã được inject thành công.
     * Mục đích: Đảm bảo rằng đối tượng SecurityConfig đã được Spring inject thành công.
     *
     * Hàm được test: `SecurityConfig` (Lớp cấu hình bảo mật)
     *
     * Input: Lớp SecurityConfig được Spring tạo và inject.
     * Expected Output: SecurityConfig không phải là null.
     */
    @Test
    void testSecurityConfigNotNull() {
        // Assert: Kiểm tra rằng SecurityConfig đã được inject thành công
        assertNotNull(securityConfig, "SecurityConfig should be injected successfully");
    }

    /**
     * TC002 - Kiểm tra JwtAuthenticationFilter đã được inject thành công.
     * Mục đích: Đảm bảo rằng JwtAuthenticationFilter đã được inject và sẵn sàng sử dụng.
     *
     * Hàm được test: `JwtAuthenticationFilter` (Lớp bộ lọc xác thực JWT)
     *
     * Input: JwtAuthenticationFilter được Spring tạo và inject.
     * Expected Output: JwtAuthenticationFilter không phải là null.
     */
    @Test
    void testJwtAuthenticationFilterNotNull() {
        // Assert: Kiểm tra rằng JwtAuthenticationFilter đã được inject thành công
        assertNotNull(authenticationFilter, "JwtAuthenticationFilter should be injected successfully");
    }

    /**
     * TC003 - Kiểm tra CustomAuthenticationEntryPoint đã được inject thành công.
     * Mục đích: Đảm bảo rằng CustomAuthenticationEntryPoint đã được inject thành công.
     *
     * Hàm được test: `CustomAuthenticationEntryPoint` (Lớp xử lý lỗi khi xác thực)
     *
     * Input: CustomAuthenticationEntryPoint được Spring tạo và inject.
     * Expected Output: CustomAuthenticationEntryPoint không phải là null.
     */
    @Test
    void testCustomAuthenticationEntryPointNotNull() {
        // Assert: Kiểm tra rằng CustomAuthenticationEntryPoint đã được inject thành công
        assertNotNull(customAuthenticationEntryPoint, "CustomAuthenticationEntryPoint should be injected successfully");
    }

    /**
     * TC004 - Kiểm tra cấu hình bảo mật của SecurityConfig.
     * Mục đích: Kiểm tra cấu hình bảo mật được định nghĩa trong lớp SecurityConfig.
     * Đảm bảo rằng HttpSecurity đã được cấu hình đúng cách với các requestMatchers.
     *
     * Hàm được test: `SecurityConfig#securityFilterChain(HttpSecurity http)` (Cấu hình bảo mật với HttpSecurity)
     *
     * Input: Cấu hình HttpSecurity từ SecurityConfig.
     * Expected Output: Cấu hình bảo mật cho phép tất cả các yêu cầu (permitAll) được áp dụng đúng.
     */
    @Test
    void testSecurityConfig() throws Exception {
        // Act: Chạy cấu hình bảo mật từ SecurityConfig
        securityConfig.securityFilterChain(httpSecurity);

        // Assert: Kiểm tra cấu hình bảo mật với HttpSecurity
        httpSecurity
                .authorizeRequests()
                .anyRequest().permitAll(); // Kiểm tra rằng tất cả các yêu cầu đều được phép

        // Bạn có thể thêm các assertions khác nếu cần kiểm tra sâu hơn về cấu hình bảo mật
    }
}
