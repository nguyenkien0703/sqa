package com.ptit.coffee_shop.config;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.repository.RoleRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho DataInitializer, kiểm tra logic khởi tạo dữ liệu gốc (roles và admin user).
 */
@SpringBootTest(properties = {
        "admin.email=admin@example.com",
        "admin.password=admin123"
})
@ActiveProfiles("test")
@Transactional
@Rollback
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Xoá dữ liệu trước mỗi test để đảm bảo trạng thái sạch sẽ.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * TC001 - Test khởi tạo dữ liệu cơ bản
     * Mô tả: Khi chưa có dữ liệu, hệ thống phải tạo đủ tất cả các Role và User admin.
     *
     * Input:
     *  - roleRepository rỗng
     *  - userRepository rỗng
     *
     * Expected Output:
     *  - Tất cả RoleEnum đều được tạo trong DB
     *  - User admin được tạo với email admin@example.com
     *  - Mật khẩu được mã hoá và khớp với "admin123"
     *  - Vai trò admin và trạng thái ACTIVE
     */
    @Test
    void testRun_ShouldInitializeRolesAndAdminUser_WhenNotExists() {
        // Act
        dataInitializer.run();

        // Assert: Kiểm tra tất cả các Role đã được tạo
        for (RoleEnum roleEnum : RoleEnum.values()) {
            Optional<Role> roleOpt = roleRepository.findByName(roleEnum);
            assertTrue(roleOpt.isPresent(), "Role should exist: " + roleEnum.name());
        }

        // Assert: Kiểm tra User admin
        Optional<User> adminOpt = userRepository.findByEmail("admin@example.com");
        assertTrue(adminOpt.isPresent(), "Admin user should be created");

        User admin = adminOpt.get();
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()), "Password should match");
        assertEquals(RoleEnum.ROLE_ADMIN, admin.getRole().getName(), "Should have ROLE_ADMIN");
        assertEquals(Status.ACTIVE, admin.getStatus(), "Admin should be ACTIVE");
    }

    /**
     * TC002 - Không tạo lại admin nếu đã tồn tại
     * Mô tả: Nếu user admin đã tồn tại, chạy lại `dataInitializer.run()` sẽ không tạo thêm người dùng mới.
     *
     * Input:
     *  - User admin đã tồn tại
     *
     * Expected Output:
     *  - Số lượng user không thay đổi sau khi run lại
     */
    @Test
    void testRun_ShouldNotCreateAdminUser_IfExists() {
        // Arrange
        dataInitializer.run();
        long userCountBefore = userRepository.count();

        // Act
        dataInitializer.run();
        long userCountAfter = userRepository.count();

        // Assert
        assertEquals(userCountBefore, userCountAfter, "No additional admin should be created");
    }

//    /**
//     * TC003 - Lỗi khi thiếu ROLE_ADMIN
//     * Mô tả: Nếu thiếu ROLE_ADMIN trong DB, hệ thống sẽ không thể tạo user admin và ném ra ngoại lệ.
//     *
//     * Input:
//     *  - Các Role khác được tạo, nhưng thiếu ROLE_ADMIN
//     *  - Chưa có User nào
//     *
//     * Expected Output:
//     *  - CoffeeShopException được ném ra với thông báo chứa "Role Admin not found"
//     */
//    @Test
//    void testRun_ShouldThrowException_WhenAdminRoleMissing() {
//        // Arrange: Tạo các Role khác ngoài ROLE_ADMIN
//        for (RoleEnum roleEnum : RoleEnum.values()) {
//            if (roleEnum != RoleEnum.ROLE_ADMIN) {
//                Role role = new Role();
//                role.setName(roleEnum);
//                roleRepository.save(role);
//            }
//        }
//        userRepository.deleteAll(); // Đảm bảo không có user
//
//        // Act & Assert
//        Exception exception = assertThrows(CoffeeShopException.class, () -> dataInitializer.run());
//        assertTrue(exception.getMessage().contains("Role Admin not found"), "Should throw exception for missing admin role");
//    }
}
