package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Brand;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private MessageBuilder messageBuilder;

    @InjectMocks
    private BrandService brandService;

    // Helper method to create Brand objects
    private Brand createBrand(Long id, String name, Status status) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setStatus(status);
        return brand;
    }

    // Helper method to create RespMessage objects
    private RespMessage createRespMessage(String code, String desc, Object data) {
        return RespMessage.builder()
                .respCode(code)
                .respDesc(desc)
                .data(data)
                .build();
    }

    // region getAllBrands()
    @Test
    void getAllBrands_WhenActiveAndInactiveExist_ReturnOnlyActive() {
        // Arrange
        Brand activeBrand = createBrand(1L, "Active Brand", Status.ACTIVE);
        Brand inactiveBrand = createBrand(2L, "Inactive Brand", Status.INACTIVE);
        List<Brand> allBrands = List.of(activeBrand, inactiveBrand);
        List<Brand> activeBrands = List.of(activeBrand);
        when(brandRepository.getAll()).thenReturn(allBrands);
        when(messageBuilder.buildSuccessMessage(activeBrands)).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", activeBrands));

        // Act
        RespMessage result = brandService.getAllBrands();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertEquals(1, ((List<?>) result.getData()).size());
        assertEquals("Active Brand", ((List<Brand>) result.getData()).get(0).getName());
        verify(brandRepository).getAll();
        verify(messageBuilder).buildSuccessMessage(activeBrands);
    }

    @Test
    void getAllBrands_WhenEmptyDB_ReturnEmptyList() {
        // Arrange
        when(brandRepository.getAll()).thenReturn(Collections.emptyList());
        when(messageBuilder.buildSuccessMessage(Collections.emptyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage result = brandService.getAllBrands();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertTrue(((List<?>) result.getData()).isEmpty());
        verify(brandRepository).getAll();
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }

    @Test
    void getAllBrands_WhenAllInactive_ReturnEmptyList() {
        // Arrange
        Brand inactiveBrand = createBrand(1L, "Inactive Brand", Status.INACTIVE);
        List<Brand> allBrands = List.of(inactiveBrand);
        when(brandRepository.getAll()).thenReturn(allBrands);
        when(messageBuilder.buildSuccessMessage(Collections.emptyList())).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", Collections.emptyList()));

        // Act
        RespMessage result = brandService.getAllBrands();

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertTrue(((List<?>) result.getData()).isEmpty());
        verify(brandRepository).getAll();
        verify(messageBuilder).buildSuccessMessage(Collections.emptyList());
    }
    // endregion

    // region updateBrand()
    @Test
    void updateBrand_WithValidData_UpdateSuccess() {
        // Arrange
        Brand existingBrand = createBrand(1L, "Old Name", Status.ACTIVE);
        Brand updatedBrandInput = createBrand(1L, "New Name", Status.ACTIVE);
        Brand updatedBrandSaved = createBrand(1L, "New Name", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(brandRepository.save(existingBrand)).thenReturn(updatedBrandSaved);
        when(messageBuilder.buildSuccessMessage(updatedBrandSaved)).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", updatedBrandSaved));

        // Act
        RespMessage result = brandService.updateBrand(1L, updatedBrandInput);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertEquals("New Name", ((Brand) result.getData()).getName());
        assertEquals(Status.ACTIVE, ((Brand) result.getData()).getStatus());
        verify(brandRepository).findById(1L);
        verify(brandRepository).findByName("New Name");
        verify(brandRepository).save(existingBrand);
        verify(messageBuilder).buildSuccessMessage(updatedBrandSaved);
    }

    @Test
    void updateBrand_WithSameName_UpdateSuccess() {
        // Arrange
        Brand existingBrand = createBrand(1L, "Same Name", Status.ACTIVE);
        Brand updatedBrandInput = createBrand(1L, "Same Name", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.findByName("Same Name")).thenReturn(Optional.of(existingBrand));
        when(brandRepository.save(existingBrand)).thenReturn(existingBrand);
        when(messageBuilder.buildSuccessMessage(existingBrand)).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", existingBrand));

        // Act
        RespMessage result = brandService.updateBrand(1L, updatedBrandInput);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertEquals("Same Name", ((Brand) result.getData()).getName());
        assertEquals(Status.ACTIVE, ((Brand) result.getData()).getStatus());
        verify(brandRepository).findById(1L);
        verify(brandRepository).findByName("Same Name");
        verify(brandRepository).save(existingBrand);
        verify(messageBuilder).buildSuccessMessage(existingBrand);
    }

    @Test
    void updateBrand_WithDuplicateName_ThrowException() {
        // Arrange
        Brand existingBrand = createBrand(1L, "Original", Status.ACTIVE);
        Brand duplicateBrand = createBrand(2L, "Duplicate", Status.ACTIVE);
        Brand updatedBrandInput = createBrand(1L, "Duplicate", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.findByName("Duplicate")).thenReturn(Optional.of(duplicateBrand));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.updateBrand(1L, updatedBrandInput));
        assertEquals(Constant.FIELD_EXISTED, exception.getCode());
        assertEquals("Brand name is duplicate", exception.getMessage());
        verify(brandRepository).findById(1L);
        verify(brandRepository).findByName("Duplicate");
        verify(brandRepository, never()).save(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void updateBrand_WithEmptyName_ThrowValidationError() {
        // Arrange
        Brand invalidBrandInput = createBrand(1L, "  ", Status.ACTIVE);

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.updateBrand(1L, invalidBrandInput));
        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertEquals("Brand name must be not null", exception.getMessage());
        verify(brandRepository, never()).findById(any());
        verify(brandRepository, never()).findByName(any());
        verify(brandRepository, never()).save(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void updateBrand_WithNullName_ThrowValidationError() {
        // Arrange
        Brand invalidBrandInput = createBrand(1L, null, Status.ACTIVE);

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.updateBrand(1L, invalidBrandInput));
        assertEquals(Constant.FIELD_NOT_NULL, exception.getCode());
        assertEquals("Brand name must be not null", exception.getMessage());
        verify(brandRepository, never()).findById(any());
        verify(brandRepository, never()).findByName(any());
        verify(brandRepository, never()).save(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void updateBrand_WhenIdNotExists_ThrowNotFoundException() {
        // Arrange
        Brand updatedBrandInput = createBrand(1L, "New Name", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.updateBrand(1L, updatedBrandInput));
        assertEquals(Constant.UNDEFINED, exception.getCode());
        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository).findById(1L);
        verify(brandRepository, never()).findByName(any());
        verify(brandRepository, never()).save(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void updateBrand_WhenSaveFails_ThrowException() {
        // Arrange
        Brand existingBrand = createBrand(1L, "Old Name", Status.ACTIVE);
        Brand updatedBrandInput = createBrand(1L, "New Name", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(brandRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.updateBrand(1L, updatedBrandInput));
        assertEquals(Constant.UNDEFINED, exception.getCode());
        assertEquals("Could not update brand", exception.getMessage());
        verify(brandRepository).findById(1L);
        verify(brandRepository).findByName("New Name");
        verify(brandRepository).save(existingBrand);
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }
    // endregion

    // region deleteBrand()
    @Test
    void deleteBrand_WhenExists_SoftDeleteSuccess() {
        // Arrange
        Brand brand = createBrand(1L, "To Delete", Status.ACTIVE);
        Brand deletedBrand = createBrand(1L, "To Delete", Status.INACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.save(brand)).thenReturn(deletedBrand);
        when(messageBuilder.buildSuccessMessage(deletedBrand)).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", deletedBrand));

        // Act
        RespMessage result = brandService.deleteBrand(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertEquals(Status.INACTIVE, ((Brand) result.getData()).getStatus());
        assertEquals("To Delete", ((Brand) result.getData()).getName());
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(brand);
        verify(messageBuilder).buildSuccessMessage(deletedBrand);
    }

    @Test
    void deleteBrand_WhenNotExists_ThrowNotFoundException() {
        // Arrange
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.deleteBrand(1L));
        assertEquals(Constant.NOT_FOUND, exception.getCode());
        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository).findById(1L);
        verify(brandRepository, never()).save(any());
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void deleteBrand_WhenSaveFails_ThrowException() {
        // Arrange
        Brand brand = createBrand(1L, "Test", Status.ACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        CoffeeShopException exception = assertThrows(CoffeeShopException.class,
                () -> brandService.deleteBrand(1L));
        assertEquals(Constant.UNDEFINED, exception.getCode());
        assertEquals("Could not delete brand", exception.getMessage());
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(brand);
        verify(messageBuilder, never()).buildSuccessMessage(any());
    }

    @Test
    void deleteBrand_WhenAlreadyInactive_SoftDeleteSuccess() {
        // Arrange
        Brand brand = createBrand(1L, "To Delete", Status.INACTIVE);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.save(brand)).thenReturn(brand);
        when(messageBuilder.buildSuccessMessage(brand)).thenReturn(
                createRespMessage(Constant.SUCCESS, "Success", brand));

        // Act
        RespMessage result = brandService.deleteBrand(1L);

        // Assert
        assertEquals(Constant.SUCCESS, result.getRespCode());
        assertEquals("Success", result.getRespDesc());
        assertEquals(Status.INACTIVE, ((Brand) result.getData()).getStatus());
        assertEquals("To Delete", ((Brand) result.getData()).getName());
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(brand);
        verify(messageBuilder).buildSuccessMessage(brand);
    }
    // endregion
}