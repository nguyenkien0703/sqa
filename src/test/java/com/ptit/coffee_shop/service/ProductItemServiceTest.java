package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Product;
import com.ptit.coffee_shop.model.ProductItem;
import com.ptit.coffee_shop.model.TypeProduct;
import com.ptit.coffee_shop.payload.request.ProductItemRequest;
import com.ptit.coffee_shop.payload.response.ProductItemResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.ProductItemRepository;
import com.ptit.coffee_shop.repository.ProductRepository;
import com.ptit.coffee_shop.repository.TypeProductRepository;
import com.ptit.coffee_shop.config.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ProductItemServiceTest {

    @Autowired
    private ProductItemService productItemService;

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TypeProductRepository typeProductRepository;

    @Autowired
    private MessageBuilder messageBuilder;

    private Product product;
    private TypeProduct typeProduct;

    // Thiết lập dữ liệu trước mỗi test case
    @BeforeEach
    void setUp() {
        // Tạo sản phẩm mẫu
        product = new Product();
        product.setName("Product 1");
        product.setDescription("Description of Product 1");
        productRepository.save(product);

        // Tạo loại sản phẩm mẫu
        typeProduct = new TypeProduct();
        typeProduct.setName("Type 1");
        typeProductRepository.save(typeProduct);
    }

    // Test case TC001: Thêm ProductItem hợp lệ
    @Test
    void testAddProductItem() {
        // Mô tả: Kiểm tra thêm ProductItem với dữ liệu hợp lệ
        // Input: ProductItemRequest với các giá trị hợp lệ
        // Expected Output: Trả về RespMessage với mã SUCCESS và ProductItem được tạo
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        RespMessage response = productItemService.addProductItem(request);

        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertNotNull(response.getData());
        ProductItem productItem = (ProductItem) response.getData();
        assertEquals(100.0, productItem.getPrice());
        assertEquals(10, productItem.getStock());
    }

    // Test case TC002: Thêm ProductItem với giá âm
    @Test
    void testAddProductItemWithInvalidPrice() {
        // Mô tả: Kiểm tra thêm ProductItem với giá âm
        // Input: ProductItemRequest với giá âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(-100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        assertTrue(exception.getMessage().contains("Price can not be negative"));
    }

    // Test case TC003: Thêm ProductItem với số lượng tồn kho âm
    @Test
    void testAddProductItemWithInvalidStock() {
        // Mô tả: Kiểm tra thêm ProductItem với số lượng tồn kho âm
        // Input: ProductItemRequest với stock âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(-10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        assertTrue(exception.getMessage().contains("Stock can not be negative"));
    }

    // Test case TC004: Thêm ProductItem với chiết khấu âm
    @Test
    void testAddProductItemWithInvalidDiscount() {
        // Mô tả: Kiểm tra thêm ProductItem với chiết khấu âm
        // Input: ProductItemRequest với discount âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(-5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        assertTrue(exception.getMessage().contains("Discount can not be negative"));
    }

    // Test case TC005: Thêm ProductItem với ID sản phẩm không hợp lệ
    @Test
    void testAddProductItemWithInvalidProductId() {
        // Mô tả: Kiểm tra thêm ProductItem với ID sản phẩm không hợp lệ
        // Input: ProductItemRequest với productId = 0
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_NULL
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(0L);
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertTrue(exception.getMessage().contains("Product id must be greater than 0"));
    }

    // Test case TC006: Thêm ProductItem với ID loại sản phẩm không hợp lệ
    @Test
    void testAddProductItemWithInvalidTypeId() {
        // Mô tả: Kiểm tra thêm ProductItem với ID loại sản phẩm không hợp lệ
        // Input: ProductItemRequest với typeId = 0
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_NULL
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(0L);
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertTrue(exception.getMessage().contains("Type id must be greater than 0"));
    }

    // Test case TC007: Thêm ProductItem với ID sản phẩm không tồn tại
    @Test
    void testAddProductItemWithNonExistentProduct() {
        // Mô tả: Kiểm tra thêm ProductItem với ID sản phẩm không tồn tại
        // Input: ProductItemRequest với productId không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(999L);
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertTrue(exception.getMessage().contains("Product id not found"));
    }

    // Test case TC008: Thêm ProductItem với ID loại sản phẩm không tồn tại
    @Test
    void testAddProductItemWithNonExistentType() {
        // Mô tả: Kiểm tra thêm ProductItem với ID loại sản phẩm không tồn tại
        // Input: ProductItemRequest với typeId không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(999L);
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.addProductItem(request);
        });

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertTrue(exception.getMessage().contains("Type id not found"));
    }

    // Test case TC009: Lấy danh sách ProductItem
    @Test
    void testGetProductItem() {
        // Mô tả: Kiểm tra lấy danh sách ProductItem theo productId
        // Input: productId hợp lệ
        // Expected Output: Trả về RespMessage với mã SUCCESS và danh sách ProductItem
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);
        productItemService.addProductItem(request);

        RespMessage response = productItemService.getProductItem(product.getId());

        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertTrue(((List<ProductItemResponse>) response.getData()).size() > 0);
    }

    // Test case TC010: Cập nhật ProductItem
    @Test
    void testUpdateProductItem() {
        // Mô tả: Kiểm tra cập nhật ProductItem với các trường hợp khác nhau
        // Input: ProductItemRequest với các giá trị khác nhau
        // Expected Output: Thành công hoặc ném CoffeeShopException tùy trường hợp
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);
        RespMessage response = productItemService.addProductItem(request);
        ProductItem productItem = (ProductItem) response.getData();

        testUpdateProductItemWithInvalidPrice(productItem);
        testUpdateProductItemWithInvalidStock(productItem);
        testUpdateProductItemWithInvalidDiscount(productItem);
        testUpdateProductItemWithInvalidProductId(productItem);
        testUpdateProductItemWithInvalidTypeId(productItem);
        testUpdateProductItemWithNonExistentProduct(productItem);
        testUpdateProductItemWithNonExistentType(productItem);
        testUpdateProductItemWithNonExistentProductItem(productItem);
        testUpdateProductItemWithValidRequest(productItem);
    }

    // Test case TC011: Cập nhật ProductItem với giá âm
    void testUpdateProductItemWithInvalidPrice(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với giá âm
        // Input: ProductItemRequest với giá âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest invalidPriceReq = new ProductItemRequest();
        invalidPriceReq.setProductId(product.getId());
        invalidPriceReq.setTypeId(typeProduct.getId());
        invalidPriceReq.setPrice(-10.0);
        invalidPriceReq.setStock(10);
        invalidPriceReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(invalidPriceReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_VALID, ex.getCode());
        assertTrue(ex.getMessage().contains("Price can not be negative"));
    }

    // Test case TC012: Cập nhật ProductItem với số lượng tồn kho âm
    void testUpdateProductItemWithInvalidStock(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với số lượng tồn kho âm
        // Input: ProductItemRequest với stock âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest invalidStockReq = new ProductItemRequest();
        invalidStockReq.setProductId(product.getId());
        invalidStockReq.setTypeId(typeProduct.getId());
        invalidStockReq.setPrice(100.0);
        invalidStockReq.setStock(-1);
        invalidStockReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(invalidStockReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_VALID, ex.getCode());
        assertTrue(ex.getMessage().contains("Stock can not be negative"));
    }

    // Test case TC013: Cập nhật ProductItem với chiết khấu âm
    void testUpdateProductItemWithInvalidDiscount(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với chiết khấu âm
        // Input: ProductItemRequest với discount âm
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_VALID
        ProductItemRequest invalidDiscountReq = new ProductItemRequest();
        invalidDiscountReq.setProductId(product.getId());
        invalidDiscountReq.setTypeId(typeProduct.getId());
        invalidDiscountReq.setPrice(100.0);
        invalidDiscountReq.setStock(10);
        invalidDiscountReq.setDiscount(-5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(invalidDiscountReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_VALID, ex.getCode());
        assertTrue(ex.getMessage().contains("Discount can not be negative"));
    }

    // Test case TC014: Cập nhật ProductItem với ID sản phẩm không hợp lệ
    void testUpdateProductItemWithInvalidProductId(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với ID sản phẩm không hợp lệ
        // Input: ProductItemRequest với productId = 0
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_NULL
        ProductItemRequest invalidProductIdReq = new ProductItemRequest();
        invalidProductIdReq.setProductId(0L);
        invalidProductIdReq.setTypeId(typeProduct.getId());
        invalidProductIdReq.setPrice(100.0);
        invalidProductIdReq.setStock(10);
        invalidProductIdReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(invalidProductIdReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_NULL, ex.getCode());
        assertTrue(ex.getMessage().contains("Product id must be greater than 0"));
    }

    // Test case TC015: Cập nhật ProductItem với ID loại sản phẩm không hợp lệ
    void testUpdateProductItemWithInvalidTypeId(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với ID loại sản phẩm không hợp lệ
        // Input: ProductItemRequest với typeId = 0
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_NULL
        ProductItemRequest invalidTypeIdReq = new ProductItemRequest();
        invalidTypeIdReq.setProductId(product.getId());
        invalidTypeIdReq.setTypeId(0L);
        invalidTypeIdReq.setPrice(100.0);
        invalidTypeIdReq.setStock(10);
        invalidTypeIdReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(invalidTypeIdReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_NULL, ex.getCode());
        assertTrue(ex.getMessage().contains("Type id must be greater than 0"));
    }

    // Test case TC016: Cập nhật ProductItem với ID sản phẩm không tồn tại
    void testUpdateProductItemWithNonExistentProduct(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với ID sản phẩm không tồn tại
        // Input: ProductItemRequest với productId không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        ProductItemRequest notFoundProductReq = new ProductItemRequest();
        notFoundProductReq.setProductId(999L);
        notFoundProductReq.setTypeId(typeProduct.getId());
        notFoundProductReq.setPrice(100.0);
        notFoundProductReq.setStock(10);
        notFoundProductReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(notFoundProductReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_FOUND, ex.getCode());
        assertTrue(ex.getMessage().contains("Product id not found"));
    }

    // Test case TC017: Cập nhật ProductItem với ID loại sản phẩm không tồn tại
    void testUpdateProductItemWithNonExistentType(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với ID loại sản phẩm không tồn tại
        // Input: ProductItemRequest với typeId không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        ProductItemRequest notFoundTypeReq = new ProductItemRequest();
        notFoundTypeReq.setProductId(product.getId());
        notFoundTypeReq.setTypeId(999L);
        notFoundTypeReq.setPrice(100.0);
        notFoundTypeReq.setStock(10);
        notFoundTypeReq.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(notFoundTypeReq, productItem.getId());
        });
        assertEquals(Constant.FIELD_NOT_FOUND, ex.getCode());
        assertTrue(ex.getMessage().contains("Type id not found"));
    }

    // Test case TC018: Cập nhật ProductItem với ID ProductItem không tồn tại
    void testUpdateProductItemWithNonExistentProductItem(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với ID ProductItem không tồn tại
        // Input: ID ProductItem không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        ProductItemRequest validRequest = new ProductItemRequest();
        validRequest.setProductId(product.getId());
        validRequest.setTypeId(typeProduct.getId());
        validRequest.setPrice(100.0);
        validRequest.setStock(10);
        validRequest.setDiscount(5.0);
        CoffeeShopException ex = assertThrows(CoffeeShopException.class, () -> {
            productItemService.updateProductItem(validRequest, 999L);
        });
        assertEquals(Constant.FIELD_NOT_FOUND, ex.getCode());
        assertTrue(ex.getMessage().contains("ProductItem not found"));
    }

    // Test case TC019: Cập nhật ProductItem với yêu cầu hợp lệ
    void testUpdateProductItemWithValidRequest(ProductItem productItem) {
        // Mô tả: Kiểm tra cập nhật ProductItem với dữ liệu hợp lệ
        // Input: ProductItemRequest với các giá trị hợp lệ
        // Expected Output: Trả về RespMessage với mã SUCCESS và ProductItem được cập nhật
        ProductItemRequest updateRequest = new ProductItemRequest();
        updateRequest.setProductId(product.getId());
        updateRequest.setTypeId(typeProduct.getId());
        updateRequest.setPrice(120.0);
        updateRequest.setStock(15);
        updateRequest.setDiscount(10.0);

        RespMessage updateResponse = productItemService.updateProductItem(updateRequest, productItem.getId());
        assertEquals(Constant.SUCCESS, updateResponse.getRespCode());

        ProductItem updatedProductItem = (ProductItem) updateResponse.getData();
        assertEquals(120.0, updatedProductItem.getPrice());
        assertEquals(15, updatedProductItem.getStock());
        assertEquals(10.0, updatedProductItem.getDiscount());
    }

    // Test case TC020: Xóa ProductItem
    @Test
    void testDeleteProductItem() {
        // Mô tả: Kiểm tra xóa ProductItem hợp lệ
        // Input: ID ProductItem hợp lệ
        // Expected Output: Trả về RespMessage với mã SUCCESS và ProductItem chuyển sang trạng thái INACTIVE
        ProductItemRequest request = new ProductItemRequest();
        request.setProductId(product.getId());
        request.setTypeId(typeProduct.getId());
        request.setPrice(100.0);
        request.setStock(10);
        request.setDiscount(5.0);
        RespMessage response = productItemService.addProductItem(request);
        ProductItem productItem = (ProductItem) response.getData();

        RespMessage deleteResponse = productItemService.deleteProductItem(productItem.getId());
        assertEquals(Constant.SUCCESS, deleteResponse.getRespCode());

        ProductItem deletedProductItem = productItemRepository.findById(productItem.getId()).orElse(null);
        assertNotNull(deletedProductItem);
        assertEquals(Status.INACTIVE, deletedProductItem.getStatus());
    }

    // Test case TC021: Xóa ProductItem với ID không tồn tại
    @Test
    void testDeleteProductItemWithNonExistentId() {
        // Mô tả: Kiểm tra xóa ProductItem với ID không tồn tại
        // Input: ID ProductItem không tồn tại
        // Expected Output: Ném CoffeeShopException với mã FIELD_NOT_FOUND
        long nonExistentId = 999L;

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            productItemService.deleteProductItem(nonExistentId);
        });

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertTrue(exception.getMessage().contains("ProductItem not found"));
    }

    // Test case TC022: Lấy ProductItem bỏ qua các mục INACTIVE
    @Test
    void testGetProductItemIgnoresInactiveItems() {
        // Mô tả: Kiểm tra lấy danh sách ProductItem chỉ trả về các mục ACTIVE
        // Input: productId hợp lệ
        // Expected Output: Trả về danh sách chỉ chứa ProductItem có trạng thái ACTIVE
        ProductItemRequest activeRequest = new ProductItemRequest();
        activeRequest.setProductId(product.getId());
        activeRequest.setTypeId(typeProduct.getId());
        activeRequest.setPrice(100.0);
        activeRequest.setStock(10);
        activeRequest.setDiscount(5.0);
        productItemService.addProductItem(activeRequest);

        ProductItem inactiveItem = ProductItem.builder()
                .product(product)
                .type(typeProduct)
                .price(200.0)
                .stock(5)
                .discount(0.0)
                .status(Status.INACTIVE)
                .build();
        productItemRepository.save(inactiveItem);

        RespMessage response = productItemService.getProductItem(product.getId());
        List<ProductItemResponse> responses = (List<ProductItemResponse>) response.getData();

        assertEquals(1, responses.size());
        assertTrue(responses.stream().noneMatch(item -> item.getPrice() == 200.0));
    }
}