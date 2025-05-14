package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.model.Brand;
import com.ptit.coffee_shop.model.Category;
import com.ptit.coffee_shop.model.Product;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.response.ProductStatisticResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.payload.response.UserStatisticResponse;
import com.ptit.coffee_shop.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @InjectMocks
    private StatisticService statisticService;

    // Helper methods
    private Product createProduct(Long id, String name, Category category, Brand brand) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategory(category);
        product.setBrand(brand);
        return product;
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Brand createBrand(Long id, String name) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        return brand;
    }

    private User createUser(Long id, String name, String email, Date creatAt) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setCreated_at(creatAt); // Giả định User có setCreatedAt, cần xác nhận
        return user;
    }

    private ProductStatisticResponse createProductStatisticResponse(Long productId, String productName, String categoryName, String brandName, Long quantity, Double revenue) {
        return new ProductStatisticResponse(productId, productName, categoryName, brandName, quantity, revenue);
    }

    private UserStatisticResponse createUserStatisticResponse(Long userId, String userName, String email, Date creatAt, Double totalSold) {
        return new UserStatisticResponse(userId, userName, email, creatAt, totalSold);
    }

    private RespMessage createRespMessage(String code, String desc, Object data) {
        return new RespMessage(code, desc, data);
    }

    // region getTop5MonthlySellingProduct
    @Test
    void getTop5MonthlySellingProduct_WithData_ShouldReturnProducts() {
        // Arrange
        Date startDate = new Date();
        Date endDate = new Date();
        Category category = createCategory(1L, "Category 1");
        Brand brand = createBrand(1L, "Brand 1");
        Product product = createProduct(1L, "Product 1", category, brand);
        Object[] result = new Object[]{product, 100L, 5000.0};
        List<Object[]> results = new ArrayList<>();
        results.add(result);
        ProductStatisticResponse response = createProductStatisticResponse(1L, "Product 1", "Category 1", "Brand 1", 100L, 5000.0);

        when(orderItemRepository.findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class)))
                .thenReturn(results);
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage respMessage = statisticService.getTop5MonthlySellingProduct(startDate, endDate);

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        List<ProductStatisticResponse> data = (List<ProductStatisticResponse>) respMessage.getData();
        assertEquals(1, data.size());
        assertEquals(1L, data.get(0).getProductId());
        assertEquals("Product 1", data.get(0).getProductName());
        assertEquals("Category 1", data.get(0).getCategoryName());
        assertEquals("Brand 1", data.get(0).getBrandName());
        assertEquals(100L, data.get(0).getQuantitySold());
        assertEquals(5000.0, data.get(0).getTotalRevenue());
        verify(orderItemRepository).findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getTop5MonthlySellingProduct_WithEmptyData_ShouldReturnEmptyList() {
        // Arrange
        Date startDate = new Date();
        Date endDate = new Date();

        when(orderItemRepository.findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage respMessage = statisticService.getTop5MonthlySellingProduct(startDate, endDate);

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        assertTrue(((List<?>) respMessage.getData()).isEmpty());
        verify(orderItemRepository).findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getTop5MonthlySellingProduct_WithNullStartDate_ShouldThrowException() {
        // Arrange
        Date endDate = new Date();

        when(orderItemRepository.findTop5MonthlySellingProducts(isNull(), same(endDate), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid date"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlySellingProduct(null, endDate));
        assertEquals("Error getting top 5 monthly selling products", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlySellingProducts(isNull(), same(endDate), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlySellingProduct_WithNullEndDate_ShouldThrowException() {
        // Arrange
        Date startDate = new Date();

        when(orderItemRepository.findTop5MonthlySellingProducts(same(startDate), isNull(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid date"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlySellingProduct(startDate, null));
        assertEquals("Error getting top 5 monthly selling products", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlySellingProducts(same(startDate), isNull(), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlySellingProduct_WithBothDatesNull_ShouldThrowException() {
        // Arrange
        when(orderItemRepository.findTop5MonthlySellingProducts(isNull(), isNull(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid date"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlySellingProduct(null, null));
        assertEquals("Error getting top 5 monthly selling products", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlySellingProducts(isNull(), isNull(), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlySellingProduct_WhenRepositoryThrowsException_ShouldThrowException() {
        // Arrange
        Date startDate = new Date();
        Date endDate = new Date();

        when(orderItemRepository.findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class)))
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlySellingProduct(startDate, endDate));
        assertEquals("Error getting top 5 monthly selling products", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlySellingProducts(same(startDate), same(endDate), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region getTop5BestSellingProduct
    @Test
    void getTop5BestSellingProduct_WithData_ShouldReturnProducts() {
        // Arrange
        Category category = createCategory(1L, "Category 1");
        Brand brand = createBrand(1L, "Brand 1");
        Product product = createProduct(1L, "Product 1", category, brand);
        Object[] result = new Object[]{product, 200L, 10000.0};
        List<Object[]> results = new ArrayList<>();
        results.add(result);
        ProductStatisticResponse response = createProductStatisticResponse(1L, "Product 1", "Category 1", "Brand 1", 200L, 10000.0);

        when(orderItemRepository.findTop5BestSellingProducts(any(Pageable.class)))
                .thenReturn(results);
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage respMessage = statisticService.getTop5BestSellingProduct();

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        List<ProductStatisticResponse> data = (List<ProductStatisticResponse>) respMessage.getData();
        assertEquals(1, data.size());
        assertEquals(1L, data.get(0).getProductId());
        assertEquals("Product 1", data.get(0).getProductName());
        assertEquals("Category 1", data.get(0).getCategoryName());
        assertEquals("Brand 1", data.get(0).getBrandName());
        assertEquals(200L, data.get(0).getQuantitySold());
        assertEquals(10000.0, data.get(0).getTotalRevenue());
        verify(orderItemRepository).findTop5BestSellingProducts(any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getTop5BestSellingProduct_WithEmptyData_ShouldReturnEmptyList() {
        // Arrange
        when(orderItemRepository.findTop5BestSellingProducts(any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage respMessage = statisticService.getTop5BestSellingProduct();

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        assertTrue(((List<?>) respMessage.getData()).isEmpty());
        verify(orderItemRepository).findTop5BestSellingProducts(any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getTop5BestSellingProduct_WhenRepositoryThrowsException_ShouldThrowException() {
        // Arrange
        when(orderItemRepository.findTop5BestSellingProducts(any(Pageable.class)))
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5BestSellingProduct());
        assertEquals("Error getting top 5 best selling products", exception.getMessage());
        verify(orderItemRepository).findTop5BestSellingProducts(any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region getTop5BestCustomers
    @Test
    void getTop5BestCustomers_WithData_ShouldReturnCustomers() {
        // Arrange
        Date creatAt = new Date();
        User user = createUser(1L, "User 1", "user1@example.com", creatAt);
        Object[] result = new Object[]{user, 1500.0};
        List<Object[]> results = new ArrayList<>();
        results.add(result);
        UserStatisticResponse response = createUserStatisticResponse(1L, "User 1", "user1@example.com", creatAt, 1500.0);

        when(orderItemRepository.findTop5BestCustomers())
                .thenReturn(results);
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage respMessage = statisticService.getTop5BestCustomers();

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        List<UserStatisticResponse> data = (List<UserStatisticResponse>) respMessage.getData();
        assertEquals(1, data.size());
        assertEquals(1L, data.get(0).getUserId());
        assertEquals("User 1", data.get(0).getUserName());
        assertEquals("user1@example.com", data.get(0).getEmail());
        assertEquals(creatAt, data.get(0).getCreatAt());
        assertEquals(1500.0, data.get(0).getTotalSold());
        verify(orderItemRepository).findTop5BestCustomers();
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getTop5BestCustomers_WithEmptyData_ShouldReturnEmptyList() {
        // Arrange
        when(orderItemRepository.findTop5BestCustomers())
                .thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage respMessage = statisticService.getTop5BestCustomers();

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        assertTrue(((List<?>) respMessage.getData()).isEmpty());
        verify(orderItemRepository).findTop5BestCustomers();
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getTop5BestCustomers_WhenRepositoryThrowsException_ShouldThrowException() {
        // Arrange
        when(orderItemRepository.findTop5BestCustomers())
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5BestCustomers());
        assertEquals("Error getting top 5 customers", exception.getMessage());
        verify(orderItemRepository).findTop5BestCustomers();
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region getTop5MonthlyCustomers
    @Test
    void getTop5MonthlyCustomers_WithData_ShouldReturnCustomers() {
        // Arrange
        int month = 10;
        int year = 2023;
        Date creatAt = new Date();
        User user = createUser(1L, "User 1", "user1@example.com", creatAt);
        Object[] result = new Object[]{user, 2000.0};
        List<Object[]> results = new ArrayList<>();
        results.add(result);
        UserStatisticResponse response = createUserStatisticResponse(1L, "User 1", "user1@example.com", creatAt, 2000.0);

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenReturn(results);
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", List.of(response)));

        // Act
        RespMessage respMessage = statisticService.getTop5MonthlyCustomers(month, year);

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        List<UserStatisticResponse> data = (List<UserStatisticResponse>) respMessage.getData();
        assertEquals(1, data.size());
        assertEquals(1L, data.get(0).getUserId());
        assertEquals("User 1", data.get(0).getUserName());
        assertEquals("user1@example.com", data.get(0).getEmail());
        assertEquals(creatAt, data.get(0).getCreatAt());
        assertEquals(2000.0, data.get(0).getTotalSold());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(anyList());
    }

    @Test
    void getTop5MonthlyCustomers_WithEmptyData_ShouldReturnEmptyList() {
        // Arrange
        int month = 10;
        int year = 2023;

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(anyList()))
                .thenReturn(createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage respMessage = statisticService.getTop5MonthlyCustomers(month, year);

        // Assert
        assertEquals(Constant.SUCCESS, respMessage.getRespCode());
        assertTrue(((List<?>) respMessage.getData()).isEmpty());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getTop5MonthlyCustomers_WithInvalidMonthZero_ShouldThrowException() {
        // Arrange
        int month = 0;
        int year = 2023;

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid month"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlyCustomers(month, year));
        assertEquals("Error getting top 5 monthly customers", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlyCustomers_WithNegativeMonth_ShouldThrowException() {
        // Arrange
        int month = -1;
        int year = 2023;

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid month"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlyCustomers(month, year));
        assertEquals("Error getting top 5 monthly customers", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlyCustomers_WithNegativeYear_ShouldThrowException() {
        // Arrange
        int month = 10;
        int year = -1;

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenThrow(new RuntimeException("Invalid year"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlyCustomers(month, year));
        assertEquals("Error getting top 5 monthly customers", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void getTop5MonthlyCustomers_WhenRepositoryThrowsException_ShouldThrowException() {
        // Arrange
        int month = 10;
        int year = 2023;

        when(orderItemRepository.findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class)))
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statisticService.getTop5MonthlyCustomers(month, year));
        assertEquals("Error getting top 5 monthly customers", exception.getMessage());
        verify(orderItemRepository).findTop5MonthlyCustomers(eq(month), eq(year), any(Pageable.class));
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion
}