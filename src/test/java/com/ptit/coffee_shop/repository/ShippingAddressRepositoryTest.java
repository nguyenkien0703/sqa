package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.ShippingAddress;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.common.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
public class ShippingAddressRepositoryTest {

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Tạo và lưu Role cho User
        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(0, RoleEnum.ROLE_USER)));

        // Tạo và lưu User mới
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

        // Tạo và lưu ShippingAddress liên kết với User
        ShippingAddress shippingAddress1 = new ShippingAddress();
        shippingAddress1.setReceiverName("John Doe");
        shippingAddress1.setReceiverPhone("1234567890");
        shippingAddress1.setLocation("123 Main Street, City A");
        shippingAddress1.setStatus(Status.ACTIVE);
        shippingAddress1.setUser(testUser);
        shippingAddressRepository.save(shippingAddress1);

        ShippingAddress shippingAddress2 = new ShippingAddress();
        shippingAddress2.setReceiverName("Jane Smith");
        shippingAddress2.setReceiverPhone("0987654321");
        shippingAddress2.setLocation("456 Another Street, City B");
        shippingAddress2.setStatus(Status.ACTIVE);
        shippingAddress2.setUser(testUser);
        shippingAddressRepository.save(shippingAddress2);
    }

    /**
     * TC001
     * Hàm: findByUser()
     * Mô tả: Kiểm tra khả năng lưu và tìm kiếm ShippingAddress theo User
     * Input: User testUser
     * Expected Output: tìm thấy 2 địa chỉ giao hàng cho User
     */
    @Test
    void testFindByUser() {
        Optional<List<ShippingAddress>> shippingAddresses = shippingAddressRepository.findByUser(testUser);

        assertThat(shippingAddresses).isPresent();
        assertThat(shippingAddresses.get().size()).isEqualTo(2);
    }

    /**
     * TC002
     * Hàm: findByUser()
     * Mô tả: Kiểm tra không có địa chỉ giao hàng cho User
     * Input: User không có địa chỉ giao hàng
     * Expected Output: Danh sách địa chỉ giao hàng trống
     */
    @Test
    void testFindByUser_WhenNoShippingAddress() {
        // Tạo User mới không có địa chỉ giao hàng
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword(passwordEncoder.encode("password"));
        newUser.setRole(roleRepository.findByName(RoleEnum.ROLE_USER).orElseThrow());
        newUser.setStatus(Status.ACTIVE);
        newUser.setName("New User");
        newUser.setPhone("987654321");
        newUser.setProfile_img("new_user_profile_url");
        newUser = userRepository.save(newUser);

        // Tìm địa chỉ giao hàng cho User mới
        Optional<List<ShippingAddress>> shippingAddresses = shippingAddressRepository.findByUser(newUser);

        // Kiểm tra xem danh sách bên trong Optional có rỗng không
        assertThat(shippingAddresses).isPresent(); // Optional không trống
        assertThat(shippingAddresses.get()).isEmpty(); // Danh sách bên trong Optional phải rỗng
    }

}
