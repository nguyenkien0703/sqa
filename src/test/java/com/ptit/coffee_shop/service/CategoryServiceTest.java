package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Category;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private RespMessage successResponse;
    private MultipartFile mockImageFile;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setStatus(Status.ACTIVE);
        testCategory.setDefaultImageUrl("http://test-image.jpg");

        successResponse = RespMessage.builder()
                .data(testCategory)
                .build();
        
        mockImageFile = new MockMultipartFile(
            "image", 
            "test.jpg",
            "image/jpeg", 
            "test image content".getBytes()
        );

        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
    }

    @Test
    public void whenGetAllCategories_thenReturnActiveCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAllCategories()).thenReturn(categories);

        // Act
        RespMessage result = categoryService.getAllCategories();

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).findAllCategories();
    }

    @Test
    public void whenGetCategoryById_withValidId_thenReturnCategory() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act
        RespMessage result = categoryService.getCategoryById(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(1L);
    }

    @Test
    public void whenGetCategoryById_withInactiveCategory_thenThrowException() {
        // Arrange
        testCategory.setStatus(Status.INACTIVE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(CoffeeShopException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    public void whenAddCategory_withValidData_thenReturnSuccess() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://test-image.jpg");
        when(cloudinaryService.upload(any(), anyString())).thenReturn(uploadResult);

        // Act
        RespMessage result = categoryService.addCategory("New Category", "Description", mockImageFile);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    public void whenAddCategory_withDuplicateName_thenThrowException() {
        // Arrange
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(CoffeeShopException.class, 
            () -> categoryService.addCategory("Test Category", "Description", null));
    }

    @Test
    public void whenUpdateCategory_withValidData_thenReturnSuccess() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://new-image.jpg");
        when(cloudinaryService.upload(any(), anyString())).thenReturn(uploadResult);

        // Act
        RespMessage result = categoryService.updateCategory(1L, "Updated Category", "Updated Description", mockImageFile);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(cloudinaryService).delete(anyString());
    }

    @Test
    public void whenDeleteCategory_thenSetStatusInactive() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        RespMessage result = categoryService.deleteCategory(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(cloudinaryService).delete(anyString());
        assertThat(testCategory.getStatus()).isEqualTo(Status.INACTIVE);
    }





} 