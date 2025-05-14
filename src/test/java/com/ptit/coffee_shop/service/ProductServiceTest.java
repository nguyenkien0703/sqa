package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.*;
import com.ptit.coffee_shop.payload.request.ProductRequest;
import com.ptit.coffee_shop.payload.response.ProductResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private TypeProductRepository typeProductRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private MessageBuilder messageBuilder;

    @InjectMocks private ProductService productService;

    // Helper methods
    private Product createProduct(Long id, String name, Status status) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setStatus(status);
        product.setCategory(new Category());
        product.setBrand(new Brand());
        product.setDescription("Test Description");
        return product;
    }

    private ProductRequest createProductRequest(String name, Long categoryId, Long brandId) {
        return new ProductRequest(name, "Description", categoryId, brandId);
    }

    private RespMessage createRespMessage(String code, String desc, Object data) {
        return new RespMessage(code, desc, data);
    }

    // region getAllProduct
    @Test
    void getAllProduct_WhenActiveAndInactiveExist_ShouldReturnOnlyActive() {
        // Arrange
        Product activeProduct = createProduct(1L, "Active Product", Status.ACTIVE);
        Product inactiveProduct = createProduct(2L, "Inactive Product", Status.INACTIVE);
        List<Product> products = List.of(activeProduct, inactiveProduct);
        ProductResponse response = new ProductResponse();
        response.setName("Active Product");
        List<Image> images = List.of(new Image());

        // Mock repository calls
        when(productRepository.findAll()).thenReturn(products);
        when(imageRepository.findByProduct(activeProduct)).thenReturn(images);

        // Mock getProductResponse to return response directly, avoiding extra calls to findByProduct
        ProductService productServiceSpy = spy(productService);
        doReturn(response).when(productServiceSpy).getProductResponse(activeProduct);

        // Mock messageBuilder
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage result = productServiceSpy.getAllProduct();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(productRepository).findAll();
        verify(imageRepository, times(1)).findByProduct(activeProduct); // Ensure exactly 1 call
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

        @Test
        void getAllProduct_WhenEmpty_ShouldReturnEmptyList() {
            // Arrange
            when(productRepository.findAll()).thenReturn(Collections.emptyList());
            when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                    createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

            // Act
            RespMessage result = productService.getAllProduct();

            // Assert
            assertEquals(Constant.SUCCESS, result.getRespCode());
            assertTrue(((List<?>) result.getData()).isEmpty());
            verify(productRepository).findAll();
            verify(imageRepository, never()).findByProduct(any());
            verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
        }
    // endregion

    // region getProductById
    @Test
    void getProductById_WhenActiveExists_ShouldReturnProduct() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        ProductResponse response = new ProductResponse();
        response.setName("Test Product");
        List<Image> images = List.of(new Image());

        // Mock repository calls
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(imageRepository.findByProduct(product)).thenReturn(images);

        // Use spy to mock getProductResponse
        ProductService productServiceSpy = spy(productService);
        doReturn(response).when(productServiceSpy).getProductResponse(product);

        // Mock messageBuilder
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", response));

        // Act
        RespMessage result = productServiceSpy.getProductById(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertNotNull(result.getData());
        verify(productRepository).findById(1L);
        verify(imageRepository, times(1)).findByProduct(product); // Ensure exactly 1 call
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void getProductById_WhenInactive_ShouldThrowException() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.INACTIVE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.getProductById(1L));
        assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        assertEquals("Product not active", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(imageRepository, never()).findByProduct(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getProductById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.getProductById(1L));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(imageRepository, never()).findByProduct(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region getProductsByCategoryId
    @Test
    void getProductsByCategoryId_WhenActiveProductsExist_ShouldReturnProducts() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        ProductResponse response = new ProductResponse();
        response.setName("Test Product");

        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage result = productService.getProductsByCategoryId(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(productRepository).findByCategoryId(1L);
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getProductsByCategoryId_WhenNoActiveProducts_ShouldReturnEmptyList() {
        // Arrange
        Product inactiveProduct = createProduct(1L, "Test Product", Status.INACTIVE);
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(inactiveProduct));
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage result = productService.getProductsByCategoryId(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertTrue(((List<?>) result.getData()).isEmpty());
        verify(productRepository).findByCategoryId(1L);
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getProductsByCategoryId_WhenError_ShouldThrowException() {
        // Arrange
        when(productRepository.findByCategoryId(1L)).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.getProductsByCategoryId(1L));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        verify(productRepository).findByCategoryId(1L);
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region searchProductsByKeyword
    @Test
    void searchProductsByKeyword_WhenMatchesFound_ShouldReturnProducts() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        ProductResponse response = new ProductResponse();
        response.setName("Test Product");

        when(productRepository.searchByKeyword("test")).thenReturn(List.of(product));
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage result = productService.searchProductsByKeyword("test");

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(productRepository).searchByKeyword("test");
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void searchProductsByKeyword_WhenNoMatches_ShouldReturnFailure() {
        // Arrange
        when(productRepository.searchByKeyword("invalid")).thenReturn(Collections.emptyList());
        when(messageBuilder.buildFailureMessage(eq(Constant.FIELD_NOT_FOUND), isNull(), isNull())).thenReturn(
                createRespMessage(Constant.FIELD_NOT_FOUND, "Not found", null));

        // Act
        RespMessage result = productService.searchProductsByKeyword("invalid");

        // Assert
        assertEquals(Constant.FIELD_NOT_FOUND, result.getRespCode());
        verify(productRepository).searchByKeyword("invalid");
        verify(messageBuilder).buildFailureMessage(eq(Constant.FIELD_NOT_FOUND), isNull(), isNull());
    }

    @Test
    void searchProductsByKeyword_WhenError_ShouldReturnFailure() {
        // Arrange
        when(productRepository.searchByKeyword("test")).thenThrow(new RuntimeException("DB Error"));
        when(messageBuilder.buildFailureMessage(eq(Constant.SYSTEM_ERROR), isNull(), isNull())).thenReturn(
                createRespMessage(Constant.SYSTEM_ERROR, "System error", null));

        // Act
        RespMessage result = productService.searchProductsByKeyword("test");

        // Assert
        assertEquals(Constant.SYSTEM_ERROR, result.getRespCode());
        verify(productRepository).searchByKeyword("test");
        verify(messageBuilder).buildFailureMessage(eq(Constant.SYSTEM_ERROR), isNull(), isNull());
    }
    // endregion

    // region addProduct
    @Test
    void addProduct_WithValidRequest_ShouldCreateProduct() {
        // Arrange
        ProductRequest request = createProductRequest("Test Product", 1L, 1L);
        Category category = new Category();
        Brand brand = new Brand();
        Product savedProduct = createProduct(1L, "Test Product", Status.ACTIVE);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", new ProductResponse()));

        // Act
        RespMessage result = productService.addProduct(request);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        verify(categoryRepository).findById(1L);
        verify(brandRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void addProduct_WithNullName_ShouldThrowException() {
        // Arrange
        ProductRequest request = createProductRequest(null, 1L, 1L);

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addProduct(request));
        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertEquals("Product name must be not null", exception.getMessage());
        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_WithInvalidCategoryId_ShouldThrowException() {
        // Arrange
        ProductRequest request = createProductRequest("Test Product", 999L, 1L);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addProduct(request));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Category id not found", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(brandRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void addProduct_WithSaveError_ShouldThrowException() {
        // Arrange
        ProductRequest request = createProductRequest("Test Product", 1L, 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(new Brand()));
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addProduct(request));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        assertEquals("Error when add product", exception.getMessage());
        verify(productRepository).save(any(Product.class));
    }
    // endregion

    // region addBrand
    @Test
    void addBrand_WithValidName_ShouldCreateBrand() {
        // Arrange
        String name = "New Brand";
        Brand savedBrand = new Brand();
        savedBrand.setName(name);

        when(brandRepository.findByName(name)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(savedBrand);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", savedBrand));

        // Act
        RespMessage result = productService.addBrand(name);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        verify(brandRepository).findByName(name);
        verify(brandRepository).save(any(Brand.class));
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void addBrand_WithNullName_ShouldThrowException() {
        // Arrange
        String name = null;

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addBrand(name));
        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertEquals("Brand name must be not null", exception.getMessage());
        verify(brandRepository, never()).findByName(any());
        verify(brandRepository, never()).save(any());
    }

    @Test
    void addBrand_WithDuplicateName_ShouldThrowException() {
        // Arrange
        String name = "Existing Brand";
        when(brandRepository.findByName(name)).thenReturn(Optional.of(new Brand()));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addBrand(name));
        assertEquals(Constant.FIELD_EXISTED, exception.getCode());
        assertEquals("Brand name is duplicate", exception.getMessage());
        verify(brandRepository).findByName(name);
        verify(brandRepository, never()).save(any());
    }

    @Test
    void addBrand_WithSaveError_ShouldThrowException() {
        // Arrange
        String name = "New Brand";
        when(brandRepository.findByName(name)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addBrand(name));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        assertEquals("Error when add brand", exception.getMessage());
        verify(brandRepository).save(any(Brand.class));
    }
    // endregion

    // region addTypeProduct
    @Test
    void addTypeProduct_WithValidName_ShouldCreateTypeProduct() {
        // Arrange
        String name = "New Type";
        TypeProduct savedType = new TypeProduct();
        savedType.setName(name);

        when(typeProductRepository.findByName(name)).thenReturn(Optional.empty());
        when(typeProductRepository.save(any(TypeProduct.class))).thenReturn(savedType);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", savedType));

        // Act
        RespMessage result = productService.addTypeProduct(name);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        verify(typeProductRepository).findByName(name);
        verify(typeProductRepository).save(any(TypeProduct.class));
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void addTypeProduct_WithEmptyName_ShouldThrowException() {
        // Arrange
        String name = "";

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addTypeProduct(name));
        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertEquals("Type product name must be not null", exception.getMessage());
        verify(typeProductRepository, never()).findByName(any());
        verify(typeProductRepository, never()).save(any());
    }

    @Test
    void addTypeProduct_WithDuplicateName_ShouldThrowException() {
        // Arrange
        String name = "Existing Type";
        when(typeProductRepository.findByName(name)).thenReturn(Optional.of(new TypeProduct()));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addTypeProduct(name));
        assertEquals(Constant.FIELD_EXISTED, exception.getCode());
        assertEquals("Type product name is duplicate", exception.getMessage());
        verify(typeProductRepository).findByName(name);
        verify(typeProductRepository, never()).save(any());
    }

    @Test
    void addTypeProduct_WithSaveError_ShouldThrowException() {
        // Arrange
        String name = "New Type";
        when(typeProductRepository.findByName(name)).thenReturn(Optional.empty());
        when(typeProductRepository.save(any(TypeProduct.class))).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.addTypeProduct(name));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        assertEquals("Error when add type product", exception.getMessage());
        verify(typeProductRepository).save(any(TypeProduct.class));
    }
    // endregion

    // region deleteProduct
    @Test
    void deleteProduct_WhenExists_ShouldSoftDelete() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        ProductResponse response = new ProductResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", response));

        // Act
        RespMessage result = productService.deleteProduct(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals(Status.INACTIVE, product.getStatus());
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.deleteProduct(1L));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }
    // endregion

    // region updateProduct
    @Test
    void updateProduct_WithValidData_ShouldUpdateFields() {
        // Arrange
        Product existingProduct = createProduct(1L, "Old Product", Status.ACTIVE);
        ProductRequest request = createProductRequest("New Product", 2L, 2L);
        Category newCategory = new Category();
        Brand newBrand = new Brand();
        ProductResponse response = new ProductResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(brandRepository.findById(2L)).thenReturn(Optional.of(newBrand));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", response));

        // Act
        RespMessage result = productService.updateProduct(1L, request);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("New Product", existingProduct.getName());
        assertEquals(newCategory, existingProduct.getCategory());
        assertEquals(newBrand, existingProduct.getBrand());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verify(brandRepository).findById(2L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_WhenNotExists_ShouldThrowException() {
        // Arrange
        ProductRequest request = createProductRequest("New Product", 1L, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.updateProduct(1L, request));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        Product existingProduct = createProduct(1L, "Test Product", Status.ACTIVE);
        ProductRequest request = createProductRequest("New Product", 999L, 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.updateProduct(1L, request));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Category not found", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }
    // endregion

    // region getAllTypeProduct
    @Test
    void getAllTypeProduct_WhenActiveExist_ShouldReturnTypes() {
        // Arrange
        TypeProduct activeType = new TypeProduct();
        activeType.setName("Active Type");
        activeType.setStatus(Status.ACTIVE);
        TypeProduct inactiveType = new TypeProduct();
        inactiveType.setStatus(Status.INACTIVE);

        when(typeProductRepository.findAll()).thenReturn(List.of(activeType, inactiveType));
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", List.of(activeType)));

        // Act
        RespMessage result = productService.getAllTypeProduct();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(typeProductRepository).findAll();
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getAllTypeProduct_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(typeProductRepository.findAll()).thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(anyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage result = productService.getAllTypeProduct();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertTrue(((List<?>) result.getData()).isEmpty());
        verify(typeProductRepository).findAll();
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }
    // endregion

    // region uploadImage
    @Test
    void uploadImage_WithValidFile_ShouldSaveImage() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".getBytes());
        Map<String, Object> cloudinaryResponse = new HashMap<>();
        cloudinaryResponse.put("secure_url", "http://test.com/image.jpg");
        ProductResponse response = new ProductResponse();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cloudinaryService.upload(file, "Product")).thenReturn(cloudinaryResponse);
        when(imageRepository.save(any(Image.class))).thenReturn(new Image());
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", response));

        // Act
        RespMessage result = productService.uploadImage(1L, file);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        verify(productRepository).findById(1L);
        verify(cloudinaryService).upload(file, "Product");
        verify(imageRepository).save(any(Image.class));
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void uploadImage_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".getBytes());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.uploadImage(1L, file));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(cloudinaryService, never()).upload(any(), any());
        verify(imageRepository, never()).save(any());
    }

    @Test
    void uploadImage_WhenUploadFails_ShouldThrowException() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        MultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "content".getBytes());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cloudinaryService.upload(file, "Product")).thenThrow(new RuntimeException("Upload Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.uploadImage(1L, file));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        assertEquals("Error when upload image", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(cloudinaryService).upload(file, "Product");
        verify(imageRepository, never()).save(any());
    }
    // endregion

    // region deleteImage
    @Test
    void deleteImage_WhenExists_ShouldRemoveImage() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        Image image = new Image();
        image.setUrl("http://test.com/image.jpg");
        image.setProduct(product);
        ProductResponse response = new ProductResponse();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(cloudinaryService.delete("http://test.com/image.jpg")).thenReturn(new HashMap<>());
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", response));

        // Act
        RespMessage result = productService.deleteImage(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        verify(imageRepository).findById(1L);
        verify(imageRepository).delete(image);
        verify(cloudinaryService).delete("http://test.com/image.jpg");
        verify(messageBuilder).buildSuccessMessage(any());
    }

    @Test
    void deleteImage_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.deleteImage(1L));
        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        assertEquals("Image not found", exception.getMessage());
        verify(imageRepository).findById(1L);
        verify(imageRepository, never()).delete(any());
        verify(cloudinaryService, never()).delete(any());
    }
    // endregion

    // region getProductResponse
    @Test
    void getProductResponse_WithAllData_ShouldAggregateCorrectly() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        Image image = new Image();
        Review review = new Review();
        review.setRating(4.0);

        when(imageRepository.findByProduct(product)).thenReturn(List.of(image));
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(review));
        when(orderItemRepository.findTotalSold(1L)).thenReturn(Optional.of(100));
        when(productRepository.maxPrice(1L)).thenReturn(Optional.of(50.0));
        when(productRepository.minPrice(1L)).thenReturn(Optional.of(30.0));

        // Act
        ProductResponse response = productService.getProductResponse(product);

        // Assert
        assertEquals("Test Product", response.getName());
        assertEquals(1, response.getImages().size());
        assertEquals(4.0, response.getRating());
        assertEquals(1, response.getTotalReview());
        assertEquals(100, response.getTotalSold());
        assertEquals(50.0, response.getMaxPrice());
        assertEquals(30.0, response.getMinPrice());
        verify(imageRepository).findByProduct(product);
        verify(reviewRepository).findByProductId(1L);
        verify(orderItemRepository).findTotalSold(1L);
        verify(productRepository).maxPrice(1L);
        verify(productRepository).minPrice(1L);
    }

    @Test
    void getProductResponse_WithNoReviews_ShouldSetZeroRating() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);

        when(imageRepository.findByProduct(product)).thenReturn(Collections.emptyList());
        when(reviewRepository.findByProductId(1L)).thenReturn(Collections.emptyList());
        when(orderItemRepository.findTotalSold(1L)).thenReturn(Optional.empty());
        when(productRepository.maxPrice(1L)).thenReturn(Optional.empty());
        when(productRepository.minPrice(1L)).thenReturn(Optional.empty());

        // Act
        ProductResponse response = productService.getProductResponse(product);

        // Assert
        assertEquals("Test Product", response.getName());
        assertTrue(response.getImages().isEmpty());
        assertEquals(0.0, response.getRating());
        assertEquals(0, response.getTotalReview());
        assertEquals(0, response.getTotalSold());
        assertEquals(0.0, response.getMaxPrice());
        assertEquals(0.0, response.getMinPrice());
    }

    @Test
    void getProductResponse_WhenError_ShouldThrowException() {
        // Arrange
        Product product = createProduct(1L, "Test Product", Status.ACTIVE);
        when(imageRepository.findByProduct(product)).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> productService.getProductResponse(product));
        assertEquals(Constant.SYSTEM_ERROR, exception.getCode());
        assertEquals("Error when get product response", exception.getMessage());
        verify(imageRepository).findByProduct(product);
    }
    // endregion
}