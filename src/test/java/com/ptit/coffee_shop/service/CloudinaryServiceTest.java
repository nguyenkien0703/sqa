package com.ptit.coffee_shop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    // Helper method to create a mock MultipartFile
    private MultipartFile createMockMultipartFile(String name, String content) throws IOException {
        return new MockMultipartFile(name, name, "image/jpeg", content.getBytes());
    }

    // region upload()
    @Test
    void upload_WithValidFile_UploadSuccess() throws IOException {
        // Arrange
        MultipartFile file = createMockMultipartFile("test.jpg", "dummy content");
        String folder = "test_folder";
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("public_id", "test_folder/test_image");
        uploadResult.put("url", "http://res.cloudinary.com/test/test_image.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // Act
        Map result = cloudinaryService.upload(file, folder);

        // Assert
        assertNotNull(result);
        assertEquals("test_folder/test_image", result.get("public_id"));
        assertEquals("http://res.cloudinary.com/test/test_image.jpg", result.get("url"));
        verify(uploader).upload(eq(file.getBytes()), argThat(map ->
                map.get("folder").equals(folder) &&
                        map.get("quality").equals("auto") &&
                        map.get("fetch_format").equals("auto")
        ));
    }

    @Test
    void upload_WhenIOException_ThrowRuntimeException() throws IOException {
        // Arrange
        MultipartFile file = createMockMultipartFile("test.jpg", "dummy content");
        String folder = "test_folder";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cloudinaryService.upload(file, folder));
        assertEquals("Image upload fail", exception.getMessage());
        verify(uploader).upload(any(byte[].class), anyMap());
    }
    // endregion

    // region delete()
    @Test
    void delete_WithValidImageUrl_DeleteSuccess() throws IOException {
        // Arrange
        String imageUrl = "http://res.cloudinary.com/test/upload/v123/test_folder/test_image.jpg";
        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "ok");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq("test_folder/test_image"), anyMap())).thenReturn(deleteResult);

        // Act
        Map result = cloudinaryService.delete(imageUrl);

        // Assert
        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(uploader).destroy(eq("test_folder/test_image"), eq(ObjectUtils.emptyMap()));
    }

    @Test
    void delete_WhenIOException_ThrowRuntimeException() throws IOException {
        // Arrange
        String imageUrl = "http://res.cloudinary.com/test/upload/v123/test_folder/test_image.jpg";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cloudinaryService.delete(imageUrl));
        assertEquals("Image delete fail", exception.getMessage());
        verify(uploader).destroy(eq("test_folder/test_image"), anyMap());
    }

    @Test
    void delete_WithInvalidImageUrl_ThrowIllegalArgumentException() {
        // Arrange
        String invalidImageUrl = "http://res.cloudinary.com/test/invalid_url.jpg";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cloudinaryService.delete(invalidImageUrl));
        assertEquals("Invalid URL format", exception.getMessage());
        verify(cloudinary, never()).uploader();
    }

    @Test
    void delete_WithUrlMissingVersion_ThrowIllegalArgumentException() {
        // Arrange
        String invalidImageUrl = "http://res.cloudinary.com/test/upload/test_folder/test_image.jpg";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cloudinaryService.delete(invalidImageUrl));
        assertEquals("Invalid URL format", exception.getMessage());
        verify(cloudinary, never()).uploader();
    }
    // endregion
}