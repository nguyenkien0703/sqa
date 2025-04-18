package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.model.ForgotPassword;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.repository.ForgotPasswordRepository;
import com.ptit.coffee_shop.repository.RoleRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ForgotPasswordServiceTest {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private ForgotPassword expiredForgotPassword;
    private ForgotPassword validForgotPassword;
    private User user;
    private User user2;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Tạo role USER nếu chưa có
        userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(0, RoleEnum.ROLE_USER)));

        // Tạo một user và lưu vào database
        user = User.builder()
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
        user = userRepository.save(user);

        // Tạo một user và lưu vào database
        user2 = User.builder()
                .email("testuser2@example.com")
                .password(passwordEncoder.encode("password"))
                .role(userRole)
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("Test User 2")
                .phone("12345678910")
                .profile_img("profile_img_url")
                .build();
        user2 = userRepository.save(user2);

        // Tạo forgot password đã hết hạn
        expiredForgotPassword = new ForgotPassword();
        expiredForgotPassword.setOtp(123456);  // Set OTP
        expiredForgotPassword.setExpirationTime(new Date(System.currentTimeMillis() - 3600 * 1000)); // 1h trước
        expiredForgotPassword.setUser(user);  // Liên kết với user
        forgotPasswordRepository.save(expiredForgotPassword);

        // Tạo forgot password vẫn còn hạn
        validForgotPassword = new ForgotPassword();
        validForgotPassword.setOtp(654321);  // Set OTP
        validForgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + 3600 * 1000)); // 1h sau
        validForgotPassword.setUser(user2);  // Liên kết với user
        forgotPasswordRepository.save(validForgotPassword);
    }

    /**
     * Test Case: TC001
     * Mô tả: Kiểm tra việc xóa các forgot password đã hết hạn.
     * Hàm được test: forgotPasswordService.deleteExpiredForgotPasswords()
     * Input: Tạo dữ liệu test với forgot password đã hết hạn và còn hạn
     * Expected Output: Forgot password đã hết hạn bị xóa, forgot password còn hạn vẫn tồn tại trong cơ sở dữ liệu.
     */
    @Test
    void testDeleteExpiredForgotPasswords() {
        // Gọi service để xoá các forgot password hết hạn
        forgotPasswordService.deleteExpiredForgotPasswords();

        // Kiểm tra rằng forgot password hết hạn đã bị xoá
        List<ForgotPassword> all = forgotPasswordRepository.findAll();

        // Kiểm tra rằng OTP đã hết hạn không còn tồn tại trong cơ sở dữ liệu
        assertFalse(all.stream().anyMatch(fp -> fp.getOtp().equals(123456)));  // Kiểm tra OTP đã hết hạn

        // Kiểm tra rằng OTP còn hạn vẫn tồn tại trong cơ sở dữ liệu
        assertTrue(all.stream().anyMatch(fp -> fp.getOtp().equals(654321)));   // Kiểm tra OTP còn hạn
    }
}
