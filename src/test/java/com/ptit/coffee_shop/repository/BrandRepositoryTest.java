package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Brand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Chỉ load các bean liên quan đến JPA (nhẹ hơn @SpringBootTest)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BrandRepositoryTest {

    @Autowired
    private BrandRepository brandRepository;

    /**
     * ✅ TC1: Test getAll() khi có cả Brand ACTIVE và INACTIVE trong database.
     * ➤ Kết quả mong đợi: chỉ những Brand có trạng thái ACTIVE được trả về.
     */
    @Test
    @Transactional
    @DisplayName("getAll() trả về chỉ các Brand có status ACTIVE")
    void test_TC1_getAllBrands_withActiveAndInactive() {
        // Given: 1 Brand ACTIVE và 1 Brand INACTIVE
        Brand activeBrand = new Brand();
        activeBrand.setName("Highlands");
        activeBrand.setStatus(Status.ACTIVE);

        Brand inactiveBrand = new Brand();
        inactiveBrand.setName("Starbucks");
        inactiveBrand.setStatus(Status.INACTIVE);

        brandRepository.saveAll(List.of(activeBrand, inactiveBrand));

        // When: Gọi phương thức getAll()
        List<Brand> activeBrands = brandRepository.getAll();

        // Then: Chỉ 1 brand (ACTIVE) được trả về
        assertNotNull(activeBrands);
        assertEquals(1, activeBrands.size());
        assertEquals(Status.ACTIVE, activeBrands.get(0).getStatus());
    }

    /**
     * ✅ TC2: Test getAll() khi tất cả Brand đều có status ACTIVE.
     * ➤ Kết quả mong đợi: trả về toàn bộ brand.
     */
    @Test
    @Transactional
    @DisplayName("getAll() trả về toàn bộ brand nếu tất cả ACTIVE")
    void test_TC2_getAllBrands_allActive() {
        // Given: 2 brand ACTIVE
        Brand b1 = new Brand();
        b1.setName("Trung Nguyen");
        b1.setStatus(Status.ACTIVE);

        Brand b2 = new Brand();
        b2.setName("Phuc Long");
        b2.setStatus(Status.ACTIVE);

        brandRepository.saveAll(List.of(b1, b2));

        // When
        List<Brand> result = brandRepository.getAll();

        // Then
        assertEquals(2, result.size());
        result.forEach(brand -> assertEquals(Status.ACTIVE, brand.getStatus()));
    }

    /**
     * ✅ TC3: Test getAll() khi không có Brand nào có status ACTIVE.
     * ➤ Kết quả mong đợi: danh sách trả về rỗng.
     */
    @Test
    @Transactional
    @DisplayName("getAll() trả về danh sách rỗng nếu không có brand ACTIVE")
    void test_TC3_getAllBrands_allInactive() {
        // Given: 2 brand INACTIVE
        Brand b1 = new Brand();
        b1.setName("ABC");
        b1.setStatus(Status.INACTIVE);

        Brand b2 = new Brand();
        b2.setName("XYZ");
        b2.setStatus(Status.INACTIVE);

        brandRepository.saveAll(List.of(b1, b2));

        // When
        List<Brand> result = brandRepository.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * ✅ TC4: Test getAll() khi database rỗng.
     * ➤ Kết quả mong đợi: danh sách trả về rỗng.
     */
    @Test
    @Transactional
    @DisplayName("getAll() trả về danh sách rỗng khi không có brand trong DB")
    void test_TC4_getAllBrands_emptyDB() {
        // When
        List<Brand> result = brandRepository.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}