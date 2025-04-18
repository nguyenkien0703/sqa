package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CategoryRepository.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;

    /**
     * Setup test data before each test.
     */
    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        category1 = new Category();
        category1.setName("Coffee");
        category1.setStatus(Status.ACTIVE);

        category2 = new Category();
        category2.setName("Tea");
        category2.setStatus(Status.INACTIVE);

        categoryRepository.save(category1);
        categoryRepository.save(category2);
    }

    /**
     * TC001
     * Hàm được test: findByName
     * Mô tả: Kiểm tra khi category tồn tại trong DB
     * Input: "Coffee"
     * Expected Output: Optional chứa category có name = "Coffee" và status = ACTIVE
     */
    @Test
    void testFindByName_WhenCategoryExists_ShouldReturnCategory() {
        Optional<Category> foundCategory = categoryRepository.findByName("Coffee");

        assertTrue(foundCategory.isPresent());
        assertEquals("Coffee", foundCategory.get().getName());
        assertEquals(Status.ACTIVE, foundCategory.get().getStatus());
    }

    /**
     * TC002
     * Hàm được test: findByName
     * Mô tả: Kiểm tra khi category không tồn tại trong DB
     * Input: "NonExistent"
     * Expected Output: Optional.empty()
     */
    @Test
    void testFindByName_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        Optional<Category> foundCategory = categoryRepository.findByName("NonExistent");

        assertFalse(foundCategory.isPresent());
    }

    /**
     * TC003
     * Hàm được test: findAllCategories
     * Mô tả: Kiểm tra chỉ trả về các category có status = ACTIVE
     * Input: Không có (sử dụng dữ liệu đã setup)
     * Expected Output: List chứa 1 category duy nhất có name = "Coffee", status = ACTIVE
     */
    @Test
    void testFindAllCategories_ShouldReturnOnlyActiveCategories() {
        List<Category> activeCategories = categoryRepository.findAllCategories();

        assertEquals(1, activeCategories.size());
        assertEquals("Coffee", activeCategories.get(0).getName());
        assertEquals(Status.ACTIVE, activeCategories.get(0).getStatus());
    }

    /**
     * TC004
     * Hàm được test: save
     * Mô tả: Kiểm tra lưu mới 1 category và lấy lại từ DB
     * Input: Category có name = "Juice", status = ACTIVE
     * Expected Output: Category được gán ID và lưu thành công, có thể tìm thấy lại bằng ID
     */
    @Test
    void testSaveCategory_ShouldPersistCategory() {
        Category newCategory = new Category();
        newCategory.setName("Juice");
        newCategory.setStatus(Status.ACTIVE);

        Category savedCategory = categoryRepository.save(newCategory);

        assertNotNull(savedCategory.getId());
        assertEquals("Juice", savedCategory.getName());
        assertEquals(Status.ACTIVE, savedCategory.getStatus());

        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());
        assertTrue(foundCategory.isPresent());
        assertEquals("Juice", foundCategory.get().getName());
    }

    /**
     * TC005
     * Hàm được test: delete
     * Mô tả: Kiểm tra xóa category khỏi DB
     * Input: category1 (Coffee)
     * Expected Output: Không thể tìm thấy category1 sau khi xóa
     */
    @Test
    void testDeleteCategory_ShouldRemoveCategory() {
        categoryRepository.delete(category1);

        Optional<Category> foundCategory = categoryRepository.findById(category1.getId());
        assertFalse(foundCategory.isPresent());
    }
}
