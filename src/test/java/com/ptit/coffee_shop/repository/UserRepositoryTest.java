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
        
        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);
        userRole = roleRepository.save(role);
    }

    //Tìm kiếm User theo Email
    @Test
    public void whenFindByEmail_thenReturnUser() {
        // Tạo user test
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    // Tìm user theo email không tồn tại
    public void whenFindByEmail_withNonExistentEmail_thenReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    // Kiểm tra sự tồn tại của user theo email
    public void whenExistsUserByEmail_withExistingEmail_thenReturnTrue() {
        // Tạo user test
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("password123");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        userRepository.save(user);

        boolean exists = userRepository.existsUserByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    //Kiểm tra sự tồn tại của user theo email
    public void whenExistsUserByEmail_withNonExistentEmail_thenReturnFalse() {
        boolean exists = userRepository.existsUserByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @Transactional
    //Cập nhật mật khẩu user
    public void whenUpdatePassword_thenPasswordIsUpdated() {
       
        User user = new User();
        user.setEmail("password@example.com");
        user.setPassword("oldPassword");
        user.setStatus(Status.ACTIVE);
        user.setRole(userRole);
        User savedUser = userRepository.save(user);
        
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("oldPassword");

      
        String newPassword = "newPassword123";
        userRepository.updatePassword(savedUser.getEmail(), newPassword);
        userRepository.flush(); // Đảm bảo changes được flush xuống DB

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        
       
        assertThat(updatedUser.getPassword())
            .as("Password should be updated to new value")
            .isEqualTo(newPassword);
    }

    @Test
    //Lấy danh sách user có role USER
    public void whenGetAllUser_thenReturnOnlyUsersWithRoleUser() {
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ROLE_ADMIN);
        roleRepository.save(adminRole);

        User normalUser = new User();
        normalUser.setEmail("user@example.com");
        normalUser.setPassword("password");
        normalUser.setStatus(Status.ACTIVE);
        normalUser.setRole(userRole);
        userRepository.save(normalUser);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setStatus(Status.ACTIVE);
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        List<User> users = userRepository.getAllUser();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getRole().getName()).isEqualTo(RoleEnum.ROLE_USER);
    }





} 