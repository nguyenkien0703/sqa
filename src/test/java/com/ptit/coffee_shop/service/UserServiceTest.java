package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.UserRequest;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.payload.response.UserDTO;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserRequest testUserRequest;
    private RespMessage successResponse;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("0123456789");
        testUser.setProfile_img("test.jpg");
        testUser.setStatus(Status.ACTIVE);
        testUser.setCreated_at(new Date());
        testUser.setUpdated_at(new Date());

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setName("Test User");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPhone("0123456789");
        testUserDTO.setProfile_img("test.jpg");
        testUserDTO.setStatus(Status.ACTIVE.toString());
        testUserDTO.setRoleName("ROLE_USER");

        testUserRequest = new UserRequest();
        testUserRequest.setName("Updated User");
        testUserRequest.setPhone("9876543210");
        testUserRequest.setProfileImg("updated.jpg");

        successResponse = RespMessage.builder()
                .respCode(Constant.SUCCESS)
                .respDesc("Success")
                .data(testUserDTO)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
    }

   
    @Test
    // Test trường hợp lấy danh sách tất cả user
    public void whenGetAllUsers_thenReturnAllUsers() {
        when(userRepository.getAllUser()).thenReturn(Arrays.asList(testUser));
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        RespMessage result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).getAllUser();
    }

 
    @Test
    // Test trường hợp lấy thông tin user thành công
    public void whenGetUserById_withValidEmail_thenReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        RespMessage result = userService.getUserById();

        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    // Test trường hợp lấy thông tin user với email không tồn tại
    public void whenGetUserById_withInvalidEmail_thenThrowException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            userService.getUserById();
        });

        assertThat(exception.getCode()).isEqualTo(Constant.FIELD_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("User not found when change password");
    }

    
    @Test
    //Test trường hợp ban user thành công
    public void whenBanUser_withValidId_thenReturnSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        RespMessage result = userService.banUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getStatus()).isEqualTo(Status.INACTIVE);
    }

    @Test
    // Test trường hợp ban user với ID không tồn tại
    public void whenBanUser_withInvalidId_thenThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.banUser(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with ID: 1");
    }

    
    @Test
    // Test trường hợp unban user thành công
    public void whenUnbanUser_withValidId_thenReturnSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        RespMessage result = userService.unbanUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    // Test trường hợp unban user với ID không tồn tại
    public void whenUnbanUser_withInvalidId_thenThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.unbanUser(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with ID: 1");
    }

    
    @Test
    // Test trường hợp cập nhật thông tin thành công
    public void whenUpdateUserInfo_withValidData_thenReturnSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        RespMessage result = userService.updateUserInfo(testUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getName()).isEqualTo(testUserRequest.getName());
        assertThat(testUser.getPhone()).isEqualTo(testUserRequest.getPhone());
        assertThat(testUser.getProfile_img()).isEqualTo(testUserRequest.getProfileImg());
    }

    @Test
    //Test trường hợp cập nhật thông tin với email không tồn tại
    public void whenUpdateUserInfo_withInvalidEmail_thenThrowException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            userService.updateUserInfo(testUserRequest);
        });

        assertThat(exception.getCode()).isEqualTo(Constant.FIELD_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("User not found when change password");
    }




} 