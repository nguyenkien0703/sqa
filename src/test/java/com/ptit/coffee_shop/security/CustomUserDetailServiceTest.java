package com.ptit.coffee_shop.security;

import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        // Initialize Role with RoleEnum.USER
        role = new Role();
        role.setId(1L);
        role.setName(RoleEnum.ROLE_USER);

        // Initialize User
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(role);
        user.setStatus(Status.ACTIVE);
    }

    @Test
    void loadUserByUsername_WithExistingEmailAndRoleUser_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        role.setName(RoleEnum.ROLE_USER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority(RoleEnum.ROLE_USER.toString())));
        // org.springframework.security.core.userdetails.User mặc định trả về true cho các phương thức này
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithExistingEmailAndRoleAdmin_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        role.setName(RoleEnum.ROLE_ADMIN);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new org.springframework.security.core.authority.SimpleGrantedAuthority(RoleEnum.ROLE_ADMIN.toString())));
        assertTrue(userDetails.isEnabled());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email));
        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String email = null;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(email));
        assertEquals("User not found with email: null", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithNullRole_ShouldReturnUserDetailsWithEmptyAuthorities() {
        // Arrange
        String email = "test@example.com";
        user.setRole(null); // Role là null, dẫn đến authorities rỗng
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isEnabled());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ShouldStillReturnEnabledUserDetails() {
        // Arrange
        String email = "test@example.com";
        user.setStatus(Status.INACTIVE); // User không active
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        // Vì CustomUserDetailsService tạo instance mới của org.springframework.security.core.userdetails.User,
        // nên isEnabled() luôn trả về true, không phụ thuộc vào status của User
        assertTrue(userDetails.isEnabled());
        verify(userRepository, times(1)).findByEmail(email);
    }
}