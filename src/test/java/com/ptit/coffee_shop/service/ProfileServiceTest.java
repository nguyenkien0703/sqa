package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.UserRequest;
import com.ptit.coffee_shop.payload.response.ProfileResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private RespMessage successResponse;
    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setName("Test User");
        testUser.setPhone("1234567890");
        testUser.setProfile_img("test.jpg");

        // Setup success response
        successResponse = RespMessage.builder()
                .respCode("00")
                .respDesc("Success")
                .build();

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
    }

    @Nested
    @DisplayName("Test getProfile")
    class GetProfileTest {
        @Test
        @DisplayName("Lấy thông tin profile thành công")
        void getProfile_Success() {
            // Input: Email của user đang đăng nhập
            // Expected: Trả về RespMessage chứa thông tin profile

            // Arrange
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.getProfile();

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(messageBuilder, times(1)).buildSuccessMessage(any(ProfileResponse.class));
        }

        @Test
        @DisplayName("Lấy thông tin profile thất bại khi không tìm thấy user")
        void getProfile_UserNotFound() {
            // Input: Email không tồn tại trong hệ thống
            // Expected: Ném ra CoffeeShopException với mã lỗi UNAUTHORIZED

            // Arrange
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> profileService.getProfile());
            assertEquals(Constant.UNAUTHORIZED, exception.getCode());
            verify(userRepository, times(1)).findByEmail(testEmail);
        }
    }

    @Nested
    @DisplayName("Test updateProfile")
    class UpdateProfileTest {
        @Test
        @DisplayName("Cập nhật profile thành công")
        void updateProfile_Success() {
            // Input: UserRequest với thông tin mới
            // Expected: Trả về RespMessage chứa thông tin profile đã cập nhật

            // Arrange
            UserRequest userRequest = new UserRequest();
            userRequest.setName("New Name");
            userRequest.setPhone("0987654321");
            userRequest.setProfileImg("new.jpg");
            userRequest.setPassword("newPassword");
            userRequest.setConfirmPassword("newPassword");

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("New Name", testUser.getName());
            assertEquals("0987654321", testUser.getPhone());
            assertEquals("new.jpg", testUser.getProfile_img());
            assertEquals("encodedPassword", testUser.getPassword());
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Cập nhật profile thất bại khi không tìm thấy user")
        void updateProfile_UserNotFound() {
            // Input: UserRequest với email không tồn tại
            // Expected: Ném ra CoffeeShopException với mã lỗi UNAUTHORIZED

            // Arrange
            UserRequest userRequest = new UserRequest();
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> profileService.updateProfile(userRequest));
            assertEquals(Constant.UNAUTHORIZED, exception.getCode());
            verify(userRepository, times(1)).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Cập nhật profile thành công khi không cập nhật password")
        void updateProfile_Success_WithoutPassword() {
            // Input: UserRequest với thông tin mới nhưng không có password
            // Expected: Trả về RespMessage chứa thông tin profile đã cập nhật, password không thay đổi

            // Arrange
            UserRequest userRequest = new UserRequest();
            userRequest.setName("New Name");
            userRequest.setPhone("0987654321");
            userRequest.setProfileImg("new.jpg");
            // Không set password và confirmPassword

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("New Name", testUser.getName());
            assertEquals("0987654321", testUser.getPhone());
            assertEquals("new.jpg", testUser.getProfile_img());
            assertNull(testUser.getPassword()); // Password không thay đổi
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Cập nhật profile thành công khi password và confirmPassword không khớp")
        void updateProfile_Success_PasswordMismatch() {
            // Input: UserRequest với password và confirmPassword không khớp
            // Expected: Trả về RespMessage chứa thông tin profile đã cập nhật, password không thay đổi

            // Arrange
            UserRequest userRequest = new UserRequest();
            userRequest.setName("New Name");
            userRequest.setPhone("0987654321");
            userRequest.setProfileImg("new.jpg");
            userRequest.setPassword("newPassword");
            userRequest.setConfirmPassword("differentPassword"); // Password không khớp

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("New Name", testUser.getName());
            assertEquals("0987654321", testUser.getPhone());
            assertEquals("new.jpg", testUser.getProfile_img());
            assertNull(testUser.getPassword()); // Password không thay đổi
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Cập nhật profile thành công khi chỉ cập nhật một số trường")
        void updateProfile_Success_PartialUpdate() {
            // Input: UserRequest chỉ cập nhật một số trường
            // Expected: Trả về RespMessage chứa thông tin profile đã cập nhật, các trường khác giữ nguyên

            // Arrange
            UserRequest userRequest = new UserRequest();
            userRequest.setName("New Name"); // Chỉ cập nhật tên
            // Không set phone, profileImg, password

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("New Name", testUser.getName());
            assertEquals("1234567890", testUser.getPhone()); // Giữ nguyên giá trị cũ
            assertEquals("test.jpg", testUser.getProfile_img()); // Giữ nguyên giá trị cũ
            assertNull(testUser.getPassword()); // Password không thay đổi
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Cập nhật profile thành công khi chỉ cập nhật password")
        void updateProfile_Success_OnlyPassword() {
            // Input: UserRequest chỉ có password và confirmPassword
            // Expected: Trả về RespMessage chứa thông tin profile, chỉ cập nhật password

            // Arrange
            UserRequest userRequest = new UserRequest();
            userRequest.setPassword("newPassword");
            userRequest.setConfirmPassword("newPassword");

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("Test User", testUser.getName()); // Giữ nguyên giá trị cũ
            assertEquals("1234567890", testUser.getPhone()); // Giữ nguyên giá trị cũ
            assertEquals("test.jpg", testUser.getProfile_img()); // Giữ nguyên giá trị cũ
            assertEquals("encodedPassword", testUser.getPassword()); // Cập nhật password
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
            verify(passwordEncoder, times(1)).encode("newPassword");
        }

        @Test
        @DisplayName("Cập nhật profile thành công khi không có thông tin cập nhật")
        void updateProfile_Success_NoUpdate() {
            // Input: UserRequest rỗng
            // Expected: Trả về RespMessage chứa thông tin profile, không có gì thay đổi

            // Arrange
            UserRequest userRequest = new UserRequest();
            // Không set bất kỳ trường nào

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateProfile(userRequest);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("Test User", testUser.getName()); // Giữ nguyên giá trị cũ
            assertEquals("1234567890", testUser.getPhone()); // Giữ nguyên giá trị cũ
            assertEquals("test.jpg", testUser.getProfile_img()); // Giữ nguyên giá trị cũ
            assertNull(testUser.getPassword()); // Password không thay đổi
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(userRepository, times(1)).save(testUser);
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("Test updateAvatar")
    class UpdateAvatarTest {
        @Test
        @DisplayName("Cập nhật avatar thành công")
        void updateAvatar_Success() {
            // Input: MultipartFile chứa ảnh mới
            // Expected: Trả về RespMessage chứa thông tin profile với avatar mới

            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            Map<String, Object> uploadResult = Map.of("secure_url", "https://example.com/new-avatar.jpg");
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(cloudinaryService.upload(any(), anyString())).thenReturn(uploadResult);
            when(messageBuilder.buildSuccessMessage(any(ProfileResponse.class))).thenReturn(successResponse);

            // Act
            RespMessage result = profileService.updateAvatar(file);

            // Assert
            assertNotNull(result);
            assertEquals(successResponse.getRespCode(), result.getRespCode());
            assertEquals(successResponse.getRespDesc(), result.getRespDesc());
            assertEquals("https://example.com/new-avatar.jpg", testUser.getProfile_img());
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(cloudinaryService, times(1)).upload(file, "Avatar");
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Cập nhật avatar thất bại khi không tìm thấy user")
        void updateAvatar_UserNotFound() {
            // Input: MultipartFile với email không tồn tại
            // Expected: Ném ra CoffeeShopException với mã lỗi UNAUTHORIZED

            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> profileService.updateAvatar(file));
            assertEquals(Constant.UNAUTHORIZED, exception.getCode());
            verify(userRepository, times(1)).findByEmail(testEmail);
        }

        @Test
        @DisplayName("Cập nhật avatar thất bại khi upload ảnh thất bại")
        void updateAvatar_UploadFailed() {
            // Input: MultipartFile không hợp lệ
            // Expected: Ném ra CoffeeShopException

            // Arrange
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(cloudinaryService.upload(any(), anyString())).thenThrow(new RuntimeException("Upload failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> profileService.updateAvatar(file));
            verify(userRepository, times(1)).findByEmail(testEmail);
            verify(cloudinaryService, times(1)).upload(file, "Avatar");
            verify(userRepository, never()).save(any());
        }
    }
}