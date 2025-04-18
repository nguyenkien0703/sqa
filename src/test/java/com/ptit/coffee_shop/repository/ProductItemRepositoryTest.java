package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.model.*;
import com.ptit.coffee_shop.common.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
public class ProductItemRepositoryTest {

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TypeProductRepository typeProductRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    private Product product;
    private TypeProduct typeProduct;
    private Category category;
    private Brand brand;

    @BeforeEach
    public void setUp() {
        // Tạo Category và Brand
        category = categoryRepository.save(new Category(1,"Category A", Status.ACTIVE, "no des", "no_image"));
        brand = brandRepository.save(new Brand(1, "Brand A", Status.ACTIVE));

        // Tạo và lưu đối tượng Product với Category và Brand
        product = productRepository.save(new Product(1,
                "Product A",
                "This is a product description",  // Description
                category,
                brand,
                Status.ACTIVE
        ));

        // Tạo và lưu TypeProduct
        typeProduct = typeProductRepository.save(new TypeProduct(1, "Type A", Status.ACTIVE));
    }

    // TC001: Kiểm tra phương thức existsByProductIdAndTypeId
    @Test
    public void testExistsByProductIdAndTypeId_ShouldReturnTrueWhenExists() {
        // Mô tả: Kiểm tra nếu có ProductItem với productId và typeId đã cho
        // Input: productId và typeId hợp lệ
        // Expected Output: true nếu ProductItem tồn tại

        // Tạo một ProductItem
        ProductItem productItem = new ProductItem();
        productItem.setProduct(product);
        productItem.setType(typeProduct);
        productItem.setPrice(100.0);
        productItem.setStock(10);
        productItem.setDiscount(5.0);
        productItem.setStatus(Status.ACTIVE);

        // Lưu ProductItem vào cơ sở dữ liệu
        productItemRepository.save(productItem);

        // Kiểm tra phương thức existsByProductIdAndTypeId
        boolean exists = productItemRepository.existsByProductIdAndTypeId(product.getId(), typeProduct.getId());

        // Kiểm tra điều kiện mong đợi
        assertTrue(exists, "ProductItem should exist with the given productId and typeId");
    }

    // TC002: Kiểm tra phương thức findByProductId
    @Test
    public void testFindByProductId_ShouldReturnListOfProductItems() {
        // Mô tả: Kiểm tra lấy danh sách ProductItem theo productId
        // Input: productId hợp lệ
        // Expected Output: danh sách ProductItem của sản phẩm đã cho

        // Tạo và lưu ProductItem
        ProductItem productItem1 = new ProductItem();
        productItem1.setProduct(product);
        productItem1.setType(typeProduct);
        productItem1.setPrice(100.0);
        productItem1.setStock(10);
        productItem1.setDiscount(5.0);
        productItem1.setStatus(Status.ACTIVE);
        productItemRepository.save(productItem1);

        ProductItem productItem2 = new ProductItem();
        productItem2.setProduct(product);
        productItem2.setType(typeProduct);
        productItem2.setPrice(150.0);
        productItem2.setStock(20);
        productItem2.setDiscount(10.0);
        productItem2.setStatus(Status.ACTIVE);
        productItemRepository.save(productItem2);

        // Lấy danh sách ProductItem theo productId
        List<ProductItem> productItems = productItemRepository.findByProductId(product.getId());

        // Kiểm tra số lượng ProductItem trả về
        assertEquals(2, productItems.size(), "There should be two ProductItems for the given productId");
    }
}
