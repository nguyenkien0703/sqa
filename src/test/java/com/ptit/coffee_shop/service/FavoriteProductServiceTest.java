package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.FavoriteProduct;
import com.ptit.coffee_shop.model.Product;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.FavoriteProductRequest;
import com.ptit.coffee_shop.payload.response.FavoriteProductResponse;
import com.ptit.coffee_shop.payload.response.ProductResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.FavoriteProductRepository;
import com.ptit.coffee_shop.repository.ProductRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FavoriteProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FavoriteProductRepository favoriteProductRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private ProductService productService;

    @InjectMocks
    private FavoriteProductService favoriteProductService;

    private User testUser;
    private Product testProduct;
    private FavoriteProduct testFavoriteProduct;
    private FavoriteProductResponse testFavoriteProductResponse;
    private ProductResponse testProductResponse;
    private RespMessage successResponse;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Set up test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setStatus(Status.ACTIVE);

        // Set up test product response
        testProductResponse = new ProductResponse();
        testProductResponse.setId(1L);
        testProductResponse.setName("Test Product");

        // Set up test favorite product
        testFavoriteProduct = FavoriteProduct.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .build();

        // Set up test favorite product response
        testFavoriteProductResponse = new FavoriteProductResponse(1L, testProductResponse, 1L);

        // Set up success response
        successResponse = RespMessage.builder()
                .data(testFavoriteProductResponse)
                .build();

        // Default mock behaviors
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
        when(productService.getProductResponse(any())).thenReturn(testProductResponse);
    }

    @Test
    public void whenGetFavoriteProducts_withValidUserId_thenReturnList() {
        // Arrange
        List<FavoriteProduct> favoriteProducts = Arrays.asList(testFavoriteProduct);
        when(favoriteProductRepository.findByUserId(1L)).thenReturn(favoriteProducts);

        // Act
        RespMessage result = favoriteProductService.getFavoriteProducts(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(favoriteProductRepository).findByUserId(1L);
    }

    @Test
    public void whenGetFavoriteProducts_withInvalidUserId_thenThrowException() {
        // Act & Assert
        assertThrows(CoffeeShopException.class, () -> favoriteProductService.getFavoriteProducts(0L));
    }

    @Test
    public void whenAddFavoriteProduct_withValidData_thenReturnSuccess() {
        // Arrange
        FavoriteProductRequest request = new FavoriteProductRequest(1L, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoriteProductRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);
        when(favoriteProductRepository.save(any())).thenReturn(testFavoriteProduct);

        // Act
        RespMessage result = favoriteProductService.addFavoriteProduct(request);

        // Assert
        assertThat(result).isNotNull();
        verify(favoriteProductRepository).save(any());
    }

    @Test
    public void whenAddFavoriteProduct_withDuplicate_thenThrowException() {
        // Arrange
        FavoriteProductRequest request = new FavoriteProductRequest(1L, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoriteProductRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThrows(CoffeeShopException.class, () -> favoriteProductService.addFavoriteProduct(request));
    }

    @Test
    public void whenRemoveFavoriteProduct_withValidData_thenReturnSuccess() {
        // Arrange
        FavoriteProductRequest request = new FavoriteProductRequest(1L, 1L);
        when(favoriteProductRepository.findByUserIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(testFavoriteProduct));

        // Act
        RespMessage result = favoriteProductService.removeFavoriteProduct(request);

        // Assert
        assertThat(result).isNotNull();
        verify(favoriteProductRepository).delete(testFavoriteProduct);
    }

    @Test
    public void whenRemoveFavoriteProduct_withNonExistent_thenThrowException() {
        // Arrange
        FavoriteProductRequest request = new FavoriteProductRequest(1L, 1L);
        when(favoriteProductRepository.findByUserIdAndProductId(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CoffeeShopException.class, () -> favoriteProductService.removeFavoriteProduct(request));
    }





} 