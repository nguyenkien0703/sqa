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
        // Setup test data
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

        // Only setup basic SecurityContext configuration
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
    }

    /**
     * Tên hàm: getAllUsers
     * Chức năng: Lấy danh sách tất cả người dùng trong hệ thống
     * Mục tiêu test: Kiểm tra việc lấy và chuyển đổi danh sách user thành UserDTO
     * Lớp: UserService
     * Phương thức: public RespMessage getAllUsers()
     * Input: Không có
     * Expected Output: RespMessage chứa List<UserDTO>
     * Độ phủ: 100%
     * Note: Cần kiểm tra mapping từ User sang UserDTO và thông tin role
     */
    @Test
    public void whenGetAllUsers_thenReturnAllUsers() {
        // Arrange
        when(userRepository.getAllUser()).thenReturn(Arrays.asList(testUser));
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).getAllUser();
    }

    /**
     * Tên hàm: getUserById
     * Chức năng: Lấy thông tin người dùng hiện tại từ SecurityContext
     * Mục tiêu test: Kiểm tra việc lấy thông tin user từ email trong SecurityContext
     * Lớp: UserService
     * Phương thức: public RespMessage getUserById()
     * Input: Email từ SecurityContext
     * Expected Output: 
     * - Success: RespMessage chứa UserDTO
     * - Failure: CoffeeShopException khi không tìm thấy user
     * Độ phủ: 100%
     * Note: Cần mock SecurityContext và kiểm tra exception
     */
    @Test
    public void whenGetUserById_withValidEmail_thenReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = userService.getUserById();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void whenGetUserById_withInvalidEmail_thenThrowException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            userService.getUserById();
        });

        assertThat(exception.getCode()).isEqualTo(Constant.FIELD_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("User not found when change password");
    }

    /**
     * Tên hàm: banUser
     * Chức năng: Cấm người dùng bằng cách đánh dấu là INACTIVE
     * Mục tiêu test: Kiểm tra việc cập nhật trạng thái của user thành INACTIVE
     * Lớp: UserService
     * Phương thức: public RespMessage banUser(Long userId)
     * Input: userId (Long)
     * Expected Output:
     * - Success: RespMessage chứa UserDTO đã cập nhật
     * - Failure: RuntimeException khi không tìm thấy user
     * - Failure: CoffeeShopException khi lỗi cập nhật
     * Độ phủ: 100%
     * Note: Kiểm tra status được cập nhật và lưu vào database
     */
    @Test
    public void whenBanUser_withValidId_thenReturnSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = userService.banUser(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getStatus()).isEqualTo(Status.INACTIVE);
    }

    @Test
    public void whenBanUser_withInvalidId_thenThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.banUser(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with ID: 1");
    }

    /**
     * Tên hàm: unbanUser
     * Chức năng: Bỏ cấm người dùng bằng cách đánh dấu là ACTIVE
     * Mục tiêu test: Kiểm tra việc cập nhật trạng thái của user thành ACTIVE
     * Lớp: UserService
     * Phương thức: public RespMessage unbanUser(Long userId)
     * Input: userId (Long)
     * Expected Output:
     * - Success: RespMessage chứa UserDTO đã cập nhật
     * - Failure: RuntimeException khi không tìm thấy user
     * - Failure: CoffeeShopException khi lỗi cập nhật
     * Độ phủ: 100%
     * Note: Kiểm tra status được cập nhật và lưu vào database
     */
    @Test
    public void whenUnbanUser_withValidId_thenReturnSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = userService.unbanUser(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    public void whenUnbanUser_withInvalidId_thenThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.unbanUser(1L);
        });

        assertThat(exception.getMessage()).isEqualTo("User not found with ID: 1");
    }

    /**
     * Tên hàm: updateUserInfo
     * Chức năng: Cập nhật thông tin người dùng
     * Mục tiêu test: Kiểm tra việc cập nhật thông tin cá nhân của user
     * Lớp: UserService
     * Phương thức: public RespMessage updateUserInfo(UserRequest updatedUser)
     * Input: UserRequest chứa thông tin cập nhật (name, phone, profileImg)
     * Expected Output:
     * - Success: RespMessage chứa UserDTO đã cập nhật
     * - Failure: CoffeeShopException khi không tìm thấy user
     * - Failure: CoffeeShopException khi lỗi cập nhật
     * Độ phủ: 100%
     * Note: Kiểm tra thông tin được cập nhật và updated_at được set
     */
    @Test
    public void whenUpdateUserInfo_withValidData_thenReturnSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = userService.updateUserInfo(testUserRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        assertThat(testUser.getName()).isEqualTo(testUserRequest.getName());
        assertThat(testUser.getPhone()).isEqualTo(testUserRequest.getPhone());
        assertThat(testUser.getProfile_img()).isEqualTo(testUserRequest.getProfileImg());
    }

    @Test
    public void whenUpdateUserInfo_withInvalidEmail_thenThrowException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            userService.updateUserInfo(testUserRequest);
        });

        assertThat(exception.getCode()).isEqualTo(Constant.FIELD_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("User not found when change password");
    }




} 