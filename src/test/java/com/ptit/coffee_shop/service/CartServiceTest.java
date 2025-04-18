package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.*;
import com.ptit.coffee_shop.payload.request.CartItemRequest;
import com.ptit.coffee_shop.payload.response.CartItemResponse;
import com.ptit.coffee_shop.payload.response.ProductItemResponse;
import com.ptit.coffee_shop.payload.response.ProductResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.CartItemRepository;
import com.ptit.coffee_shop.repository.ImageRepository;
import com.ptit.coffee_shop.repository.ProductItemRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private ProductItemRepository productItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private ProductItem testProductItem;
    private CartItem testCartItem;
    private Product testProduct;
    private CartItemRequest validCartItemRequest;
    private RespMessage successResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(1L)
                .name("testUser")
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Coffee")
                .description("Test Description")
                .build();

        // Setup test product item
        testProductItem = ProductItem.builder()
                .id(1L)
                .price(10.0)
                .stock(100)
                .product(testProduct)
                .build();

        // Setup test cart item
        testCartItem = CartItem.builder()
                .id(1L)
                .user(testUser)
                .productItem(testProductItem)
                .quantity(1)
                .build();

        // Setup valid cart item request
        validCartItemRequest = new CartItemRequest();
        validCartItemRequest.setUserId(1L);
        validCartItemRequest.setProductItemId(1L);
        validCartItemRequest.setQuantity(1);

        // Setup success response template
        successResponse = RespMessage.builder()
                .respCode(Constant.SUCCESS)
                .respDesc("Success")
                .data(null)
                .build();
    }

    @Nested
    @DisplayName("Test addCartItem")
    class AddCartItemTest {
        @Test
        @DisplayName("Thêm sản phẩm mới vào giỏ hàng thành công")
        void addCartItem_Success_NewItem() {
            // Input: CartItemRequest hợp lệ với sản phẩm chưa có trong giỏ
            // Expected: Thêm thành công và trả về RespMessage với code 000

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
            when(productService.getProductResponse(any())).thenReturn(new ProductResponse());

            // Act
            RespMessage result = cartService.addCartItem(validCartItemRequest);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartItemRepository, times(1)).findByUserIdAndProductItemId(1L, 1L);
        }

        @Test
        @DisplayName("Cập nhật số lượng sản phẩm đã có trong giỏ hàng")
        void addCartItem_Success_ExistingItem() {
            // Input: CartItemRequest với sản phẩm đã có trong giỏ
            // Expected: Cập nhật số lượng thành công và trả về RespMessage với code 000

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.of(testCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
            when(productService.getProductResponse(any())).thenReturn(new ProductResponse());

            // Act
            RespMessage result = cartService.addCartItem(validCartItemRequest);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).save(any(CartItem.class));
            assertEquals(2, testCartItem.getQuantity()); // Quantity should be incremented
        }

        @Test
        @DisplayName("Thêm sản phẩm với số lượng không hợp lệ")
        void addCartItem_InvalidQuantity() {
            // Input: CartItemRequest với quantity <= 0
            // Expected: Ném ra CoffeeShopException với code 101

            // Arrange
            CartItemRequest invalidRequest = new CartItemRequest();
            invalidRequest.setUserId(1L);
            invalidRequest.setProductItemId(1L);
            invalidRequest.setQuantity(0);

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.addCartItem(invalidRequest));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }

        @Test
        @DisplayName("Thêm sản phẩm với userId không hợp lệ")
        void addCartItem_InvalidUserId() {
            // Input: CartItemRequest với userId <= 0
            // Expected: Ném ra CoffeeShopException với code 101

            // Arrange
            CartItemRequest invalidRequest = new CartItemRequest();
            invalidRequest.setUserId(0L);
            invalidRequest.setProductItemId(1L);
            invalidRequest.setQuantity(1);

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.addCartItem(invalidRequest));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }

        @Test
        @DisplayName("Thêm sản phẩm với productItemId không hợp lệ")
        void addCartItem_InvalidProductItemId() {
            // Input: CartItemRequest với productItemId <= 0
            // Expected: Ném ra CoffeeShopException với code 101

            // Arrange
            CartItemRequest invalidRequest = new CartItemRequest();
            invalidRequest.setUserId(1L);
            invalidRequest.setProductItemId(0L);
            invalidRequest.setQuantity(1);

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.addCartItem(invalidRequest));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }

        @Test
        @DisplayName("Thêm sản phẩm không tồn tại")
        void addCartItem_ProductNotFound() {
            // Input: CartItemRequest với productItemId không tồn tại
            // Expected: Ném ra CoffeeShopException với code 103

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.addCartItem(validCartItemRequest));
            assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        }

        @Test
        @DisplayName("Thêm sản phẩm với user không tồn tại")
        void addCartItem_UserNotFound() {
            // Input: CartItemRequest với userId không tồn tại
            // Expected: Ném ra CoffeeShopException với code 103

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.addCartItem(validCartItemRequest));
            assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        }
    }

    @Nested
    @DisplayName("Test getCartItems")
    class GetCartItemsTest {
        @Test
        @DisplayName("Lấy danh sách sản phẩm trong giỏ hàng thành công")
        void getCartItems_Success() {
            // Input: userId hợp lệ
            // Expected: Trả về RespMessage với code 000 và danh sách sản phẩm

            // Arrange
            List<CartItem> cartItems = Collections.singletonList(testCartItem);
            when(cartItemRepository.findByUserId(1L)).thenReturn(cartItems);
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);
            when(productService.getProductResponse(any())).thenReturn(new ProductResponse());

            // Act
            RespMessage result = cartService.getCartItems(1L);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Lấy giỏ hàng trống")
        void getCartItems_EmptyCart() {
            // Input: userId hợp lệ nhưng giỏ hàng trống
            // Expected: Trả về RespMessage với code 000 và danh sách rỗng

            // Arrange
            when(cartItemRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
            when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

            // Act
            RespMessage result = cartService.getCartItems(1L);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Lấy giỏ hàng với userId không hợp lệ")
        void getCartItems_InvalidUserId() {
            // Input: userId <= 0
            // Expected: Ném ra CoffeeShopException với code 101

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.getCartItems(0L));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }
    }

    @Nested
    @DisplayName("Test updateCartItem")
    class UpdateCartItemTest {
        @Test
        @DisplayName("Cập nhật số lượng sản phẩm thành công")
        void updateCartItem_Success() {
            // Input: CartItemRequest hợp lệ
            // Expected: Cập nhật thành công và trả về RespMessage với code 000

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.of(testCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
            when(messageBuilder.buildSuccessMessage(any(String.class))).thenReturn(successResponse);

            // Act
            RespMessage result = cartService.updateCartItem(validCartItemRequest);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Cập nhật số lượng vượt quá tồn kho")
        void updateCartItem_QuantityExceedsStock() {
            // Input: CartItemRequest với quantity > stock
            // Expected: Ném ra CoffeeShopException với code 101

            // Arrange
            CartItemRequest invalidRequest = new CartItemRequest();
            invalidRequest.setUserId(1L);
            invalidRequest.setProductItemId(1L);
            invalidRequest.setQuantity(101); // Stock is 100

            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.of(testCartItem));

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.updateCartItem(invalidRequest));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }

        @Test
        @DisplayName("Cập nhật sản phẩm không tồn tại trong giỏ")
        void updateCartItem_ItemNotFound() {
            // Input: CartItemRequest với sản phẩm không có trong giỏ
            // Expected: Ném ra CoffeeShopException với code 103

            // Arrange
            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.updateCartItem(validCartItemRequest));
            assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        }

        @Test
        @DisplayName("Cập nhật với số lượng không hợp lệ")
        void updateCartItem_InvalidQuantity() {
            // Input: CartItemRequest với quantity <= 0
            // Expected: Ném ra CoffeeShopException với code 101

            // Arrange
            CartItemRequest invalidRequest = new CartItemRequest();
            invalidRequest.setUserId(1L);
            invalidRequest.setProductItemId(1L);
            invalidRequest.setQuantity(0);

            when(productItemRepository.findById(1L)).thenReturn(Optional.of(testProductItem));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cartItemRepository.findByUserIdAndProductItemId(1L, 1L)).thenReturn(Optional.of(testCartItem));

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.updateCartItem(invalidRequest));
            assertEquals(Constant.FIELD_NOT_VALID, exception.getCode());
        }
    }

    @Nested
    @DisplayName("Test deleteCartItem")
    class DeleteCartItemTest {
        @Test
        @DisplayName("Xóa sản phẩm khỏi giỏ hàng thành công")
        void deleteCartItem_Success() {
            // Input: itemId hợp lệ
            // Expected: Xóa thành công và trả về RespMessage với code 000

            // Arrange
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
            when(messageBuilder.buildSuccessMessage(any(String.class))).thenReturn(successResponse);

            // Act
            RespMessage result = cartService.deleteCartItem(1L);

            // Assert
            assertNotNull(result);
            assertEquals(Constant.SUCCESS, result.getRespCode());
            verify(cartItemRepository).delete(testCartItem);
        }

        @Test
        @DisplayName("Xóa sản phẩm không tồn tại")
        void deleteCartItem_ItemNotFound() {
            // Input: itemId không tồn tại
            // Expected: Ném ra CoffeeShopException với code 103

            // Arrange
            when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                    () -> cartService.deleteCartItem(1L));
            assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
        }
    }

    @Nested
    @DisplayName("Test toProductItemResponse")
    class ToProductItemResponseTest {
        @Test
        @DisplayName("Chuyển đổi ProductItem thành ProductItemResponse thành công")
        void toProductItemResponse_Success() {
            // Input: ProductItem hợp lệ
            // Expected: Trả về ProductItemResponse với đầy đủ thông tin

            // Arrange
            ProductResponse productResponse = new ProductResponse();
            productResponse.setId(1L);
            productResponse.setName("Test Coffee");
            when(productService.getProductResponse(any())).thenReturn(productResponse);

            // Act
            ProductItemResponse result = cartService.toProductItemResponse(testProductItem);

            // Assert
            assertNotNull(result);
            assertEquals(testProductItem.getId(), result.getId());
            assertEquals(testProductItem.getPrice(), result.getPrice());
            assertEquals(testProductItem.getStock(), result.getStock());
            assertEquals(testProductItem.getStatus(), result.getStatus());
            assertEquals(testProductItem.getType(), result.getType());
            assertNotNull(result.getProductResponse());
            assertEquals(productResponse.getId(), result.getProductResponse().getId());
        }
    }
}