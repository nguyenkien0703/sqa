package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.*;
import com.ptit.coffee_shop.payload.request.ReviewRequet;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.payload.response.ReviewResponse;
import com.ptit.coffee_shop.repository.OrderItemRepository;
import com.ptit.coffee_shop.repository.ProductItemRepository;
import com.ptit.coffee_shop.repository.ProductRepository;
import com.ptit.coffee_shop.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private OrderItem testOrderItem;
    private Product testProduct;
    private ProductItem testProductItem;
    private ReviewRequet testReviewRequest;
    private ReviewResponse testReviewResponse;
    private RespMessage successResponse;
    private Order testOrder;
    private User testUser;
    private ShippingAddress testShippingAddress;

    @BeforeEach
    public void setup() {
        // Setup test data
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testProductItem = new ProductItem();
        testProductItem.setId(1L);
        testProductItem.setProduct(testProduct);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setProfile_img("test.jpg");

        testShippingAddress = new ShippingAddress();
        testShippingAddress.setId(1L);
        testShippingAddress.setUser(testUser);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setShippingAddress(testShippingAddress);

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProductItem(testProductItem);
        testOrderItem.setReviewed(false);
        testOrderItem.setOrder(testOrder);

        testReview = new Review();
        testReview.setId(1L);
        testReview.setRating(5);
        testReview.setComment("Great product!");
        testReview.setOrderItem(testOrderItem);
        testReview.setStatus(Status.ACTIVE);
        testReview.setCreateAt(new Date());

        testReviewRequest = new ReviewRequet();
        testReviewRequest.setOrderItemId(1L);
        testReviewRequest.setRating(5);
        testReviewRequest.setComment("Great product!");

        testReviewResponse = new ReviewResponse();
        testReviewResponse.setId(1L);
        testReviewResponse.setRating(5);
        testReviewResponse.setComment("Great product!");
        testReviewResponse.setUserEmail("test@example.com");
        testReviewResponse.setUserId(1L);
        testReviewResponse.setName("Test User");
        testReviewResponse.setUserAvatar("test.jpg");
        testReviewResponse.setCreateAt(new Date());

        successResponse = RespMessage.builder()
                .respCode(Constant.SUCCESS)
                .respDesc("Success")
                .data(testReviewResponse)
                .build();
    }

    /**
     * Tên hàm: addReview
     * Chức năng: Thêm đánh giá mới cho một sản phẩm từ đơn hàng
     * Mục tiêu test: Kiểm tra việc thêm đánh giá mới
     * Lớp: ReviewService
     * Phương thức: public RespMessage addReview(ReviewRequet reviewRequet)
     * Input: ReviewRequet chứa thông tin đánh giá (orderItemId, rating, comment)
     * Expected Output:
     * - Success: RespMessage chứa ReviewResponse của đánh giá mới
     * - Failure: CoffeeShopException khi không tìm thấy OrderItem hoặc lỗi khi lưu
     * Độ phủ: 100%
     */
    @Test
    public void whenAddReview_withValidData_thenReturnSuccess() {
        // Arrange
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(testOrderItem));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = reviewService.addReview(testReviewRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(orderItemRepository).findById(1L);
        verify(orderItemRepository).save(any(OrderItem.class));
        verify(reviewRepository).save(any(Review.class));
        assertThat(testOrderItem.isReviewed()).isTrue();
    }

    @Test
    public void whenAddReview_withNonExistentOrderItem_thenThrowException() {
        // Arrange
        when(orderItemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            reviewService.addReview(testReviewRequest);
        });

        assertThat(exception.getCode()).isEqualTo(Constant.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("OrderItem could not be found");
    }

    /**
     * Tên hàm: getAllReviews
     * Chức năng: Lấy tất cả các đánh giá trong hệ thống
     * Mục tiêu test: Kiểm tra việc lấy danh sách tất cả đánh giá
     * Lớp: ReviewService
     * Phương thức: public RespMessage getAllReviews()
     * Input: Không có input
     * Expected Output: RespMessage chứa danh sách ReviewResponse
     * Độ phủ: 100%
     */
    @Test
    public void whenGetAllReviews_thenReturnAllReviews() {
        // Arrange
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findAll()).thenReturn(reviews);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = reviewService.getAllReviews();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(reviewRepository).findAll();
    }

    /**
     * Tên hàm: deleteReview
     * Chức năng: Xóa đánh giá bằng cách đánh dấu là INACTIVE
     * Mục tiêu test: Kiểm tra việc xóa đánh giá
     * Lớp: ReviewService
     * Phương thức: public RespMessage deleteReview(long reviewId)
     * Input: reviewId (long)
     * Expected Output:
     * - Success: RespMessage chứa ReviewResponse của đánh giá đã xóa
     * - Failure: CoffeeShopException khi không tìm thấy Review hoặc lỗi khi lưu
     * Độ phủ: 100%
     */
    @Test
    public void whenDeleteReview_withValidId_thenReturnSuccess() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = reviewService.deleteReview(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
        assertThat(testReview.getStatus()).isEqualTo(Status.INACTIVE);
    }

    @Test
    public void whenDeleteReview_withNonExistentId_thenThrowException() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            reviewService.deleteReview(1L);
        });

        assertThat(exception.getCode()).isEqualTo(Constant.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Review could not be found");
    }

    /**
     * Tên hàm: getReviewByProductId
     * Chức năng: Lấy danh sách đánh giá theo ID sản phẩm
     * Mục tiêu test: Kiểm tra việc lấy danh sách đánh giá theo sản phẩm
     * Lớp: ReviewService
     * Phương thức: public RespMessage getReviewByProductId(long productId)
     * Input: productId (long)
     * Expected Output:
     * - Success: RespMessage chứa danh sách ReviewResponse của sản phẩm
     * - Failure: CoffeeShopException khi không tìm thấy đánh giá
     * Độ phủ: 100%
     */
    @Test
    public void whenGetReviewByProductId_withValidId_thenReturnReviews() {
        // Arrange
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByProductId(1L)).thenReturn(reviews);
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = reviewService.getReviewByProductId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(reviewRepository).findByProductId(1L);
    }

    @Test
    public void whenGetReviewByProductId_withNoReviews_thenReturnEmptyList() {
        // Arrange
        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList());
        when(messageBuilder.buildSuccessMessage(any())).thenReturn(successResponse);

        // Act
        RespMessage result = reviewService.getReviewByProductId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRespCode()).isEqualTo(Constant.SUCCESS);
        verify(reviewRepository).findByProductId(1L);
    }

    @Test
    public void whenGetReviewByProductId_withException_thenThrowException() {
        // Arrange
        when(reviewRepository.findByProductId(1L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () -> {
            reviewService.getReviewByProductId(1L);
        });

        assertThat(exception.getCode()).isEqualTo(Constant.SYSTEM_ERROR);
        assertThat(exception.getMessage()).isEqualTo("Review not found");
    }




} 