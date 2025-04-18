package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Tạo role cho test
        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);
        userRole = roleRepository.save(role);
    }

    @Test
    public void whenFindByEmail_thenReturnUser() {
        // Tạo user test
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        userRepository.save(user);

        // Thực hiện test
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Kiểm tra kết quả
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void whenFindByEmail_withNonExistentEmail_thenReturnEmpty() {
        // Thực hiện test với email không tồn tại
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Kiểm tra kết quả
        assertThat(found).isEmpty();
    }

    @Test
    public void whenExistsUserByEmail_withExistingEmail_thenReturnTrue() {
        // Tạo user test
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("password123");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        userRepository.save(user);

        // Thực hiện test
        boolean exists = userRepository.existsUserByEmail("exists@example.com");

        // Kiểm tra kết quả
        assertThat(exists).isTrue();
    }

    @Test
    public void whenExistsUserByEmail_withNonExistentEmail_thenReturnFalse() {
        // Thực hiện test
        boolean exists = userRepository.existsUserByEmail("nonexistent@example.com");

        // Kiểm tra kết quả
        assertThat(exists).isFalse();
    }

    @Test
    @Transactional
    public void whenUpdatePassword_thenPasswordIsUpdated() {
        // Tạo user test
        User user = new User();
        user.setEmail("password@example.com");
        user.setPassword("oldPassword");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        User savedUser = userRepository.save(user);
        
        // Đảm bảo user được lưu
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("oldPassword");

        // Thực hiện update password
        String newPassword = "newPassword123";
        userRepository.updatePassword(savedUser.getEmail(), newPassword);
        userRepository.flush(); // Đảm bảo changes được flush xuống DB

        // Refresh data từ DB
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        
        // Kiểm tra kết quả
        assertThat(updatedUser.getPassword())
            .as("Password should be updated to new value")
            .isEqualTo(newPassword);
    }

    @Test
    public void whenGetAllUser_thenReturnOnlyUsersWithRoleUser() {
        // Tạo role ADMIN
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ROLE_ADMIN);
        roleRepository.save(adminRole);

        // Tạo user với role USER
        User normalUser = new User();
        normalUser.setEmail("user@example.com");
        normalUser.setPassword("password");
        normalUser.setStatus(Status.ACTIVE);
        normalUser.setRole(userRole);
        userRepository.save(normalUser);

        // Tạo user với role ADMIN
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setStatus(Status.ACTIVE);
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        // Thực hiện test
        List<User> users = userRepository.getAllUser();

        // Kiểm tra kết quả
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getRole().getName()).isEqualTo(RoleEnum.ROLE_USER);
    }





} 