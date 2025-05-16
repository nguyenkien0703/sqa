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
    //Lấy danh sách tất cả category đang active
    public void whenGetAllCategories_thenReturnActiveCategories() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAllCategories()).thenReturn(categories);

        RespMessage result = categoryService.getAllCategories();

        assertThat(result).isNotNull();
        verify(categoryRepository).findAllCategories();
    }

    @Test
    //Lấy thông tin category theo ID
    public void whenGetCategoryById_withValidId_thenReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        RespMessage result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        verify(categoryRepository).findById(1L);
    }

    @Test
    //Xử lý trường hợp category không active
    public void whenGetCategoryById_withInactiveCategory_thenThrowException() {
        testCategory.setStatus(Status.INACTIVE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        assertThrows(CoffeeShopException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    //Thêm mới category
    public void whenAddCategory_withValidData_thenReturnSuccess() {
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://test-image.jpg");
        when(cloudinaryService.upload(any(), anyString())).thenReturn(uploadResult);

        RespMessage result = categoryService.addCategory("New Category", "Description", mockImageFile);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    //Xử lý trùng tên category
    public void whenAddCategory_withDuplicateName_thenThrowException() {
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(testCategory));

        assertThrows(CoffeeShopException.class, 
            () -> categoryService.addCategory("Test Category", "Description", null));
    }

    @Test
    //Cập nhật thông tin category
    public void whenUpdateCategory_withValidData_thenReturnSuccess() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://new-image.jpg");
        when(cloudinaryService.upload(any(), anyString())).thenReturn(uploadResult);

        RespMessage result = categoryService.updateCategory(1L, "Updated Category", "Updated Description", mockImageFile);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(cloudinaryService).delete(anyString());
    }

    @Test
    //Xóa mềm category
    public void whenDeleteCategory_thenSetStatusInactive() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        RespMessage result = categoryService.deleteCategory(1L);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(cloudinaryService).delete(anyString());
        assertThat(testCategory.getStatus()).isEqualTo(Status.INACTIVE);
    }





} 