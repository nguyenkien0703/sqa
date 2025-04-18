package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.ForgotPassword;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional // đảm bảo rollback sau mỗi test
public class ForgotPasswordRepositoryTest {

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;


    @BeforeEach
    void setUp() {
        // Setup dữ liệu cho user test
        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(0, RoleEnum.ROLE_USER)));

        testUser = User.builder()
                .email("testuser@example.com")
//                .username("testuser") // thêm username để dùng trong các test
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

    /**
     * TC001
     * Hàm: findByUser()
     * Mô tả: Kiểm tra khả năng lưu và tìm kiếm ForgotPassword theo User
     * Input: User testUser, otp = 123456
     * Expected output: tìm thấy bản ghi với đúng otp
     */
    @Test
    void testSaveAndFindByUser() {
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setOtp(123456);
        forgotPassword.setUser(testUser);
        forgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + 100000));
        forgotPasswordRepository.save(forgotPassword);

        Optional<ForgotPassword> found = forgotPasswordRepository.findByUser(testUser);
        assertThat(found).isPresent();
        assertThat(found.get().getOtp()).isEqualTo(123456);
    }

    /**
     * TC002
     * Hàm: deleteByExpirationTimeBefore()
     * Mô tả: Kiểm tra xóa các bản ghi ForgotPassword đã hết hạn
     * Input: ForgotPassword với expirationTime = thời điểm quá khứ
     * Expected output: bản ghi bị xóa và không tìm thấy nữa
     */
    @Test
    void testDeleteByExpirationTimeBefore() {
        ForgotPassword expired = new ForgotPassword();
        expired.setOtp(111111);
        expired.setUser(testUser);
        expired.setExpirationTime(new Date(System.currentTimeMillis() - 100000));
        forgotPasswordRepository.save(expired);

        forgotPasswordRepository.deleteByExpirationTimeBefore(new Date());

        Optional<ForgotPassword> found = forgotPasswordRepository.findByUser(testUser);
        assertThat(found).isEmpty();
    }

    /**
     * TC003
     * Hàm: deleteByUser()
     * Mô tả: Kiểm tra xóa bản ghi ForgotPassword theo User
     * Input: User testUser
     * Expected output: bản ghi bị xóa và không thể tìm lại được
     */
    @Test
    void testDeleteByUser() {
        ForgotPassword fp = new ForgotPassword();
        fp.setOtp(222222);
        fp.setUser(testUser);
        fp.setExpirationTime(new Date(System.currentTimeMillis() + 100000));
        forgotPasswordRepository.save(fp);

        forgotPasswordRepository.deleteByUser(testUser);

        Optional<ForgotPassword> found = forgotPasswordRepository.findByUser(testUser);
        assertThat(found).isEmpty();
    }

    /**
     * TC004
     * Hàm: findByOtpAndUser()
     * Mô tả: Kiểm tra tìm kiếm bản ghi ForgotPassword theo OTP và User
     * Input: otp = 999999, User testUser
     * Expected output: tìm thấy bản ghi có otp 999999 và đúng email user
     */
    @Test
    void testFindByOtpAndUser() {
        ForgotPassword fp = new ForgotPassword();
        fp.setOtp(999999);
        fp.setUser(testUser);
        fp.setExpirationTime(new Date(System.currentTimeMillis() + 500000));
        forgotPasswordRepository.save(fp);

        Optional<ForgotPassword> found = forgotPasswordRepository.findByOtpAndUser(999999, testUser);
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("testuser@example.com");
    }
}
