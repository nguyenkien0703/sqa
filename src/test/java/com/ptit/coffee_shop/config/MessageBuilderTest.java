package com.ptit.coffee_shop.config;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.payload.response.RespMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageBuilderTest {

    @InjectMocks
    private MessageBuilder messageBuilder;

    @Mock
    private MessageSource messageSource;

    private MockedStatic<LocaleContextHolder> mockedLocaleContextHolder;

    private Locale locale;

    @BeforeEach
    void setUp() {
        // Mock LocaleContextHolder to control the locale
        mockedLocaleContextHolder = mockStatic(LocaleContextHolder.class);
        locale = Locale.US;
        mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(locale);
    }

    @Test
    void buildSuccessMessage_WithValidData_ShouldReturnSuccessMessage() {
        // Arrange
        Object data = new Object();
        String expectedDesc = "Operation successful";
        when(messageSource.getMessage(eq(Constant.SUCCESS), eq(null), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildSuccessMessage(data);

        // Assert
        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @Test
    void buildSuccessMessage_WithNullData_ShouldReturnSuccessMessage() {
        // Arrange
        Object data = null;
        String expectedDesc = "Operation successful";
        when(messageSource.getMessage(eq(Constant.SUCCESS), eq(null), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildSuccessMessage(data);

        // Assert
        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertNull(response.getData());
    }

    @Test
    void buildSuccessMessage_WhenMessageNotFound_ShouldReturnUndefinedMessage() {
        // Arrange
        Object data = new Object();
        when(messageSource.getMessage(eq(Constant.SUCCESS), eq(null), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(Constant.UNDEFINED);

        // Act
        RespMessage response = messageBuilder.buildSuccessMessage(data);

        // Assert
        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertEquals(Constant.UNDEFINED, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @Test
    void buildFailureMessage_WithValidCodeAndData_ShouldReturnFailureMessage() {
        // Arrange
        String code = "ERROR_CODE";
        Object[] objects = new Object[]{"param1", "param2"};
        Object data = new Object();
        String expectedDesc = "Error occurred with param1 and param2";
        when(messageSource.getMessage(eq(code), eq(objects), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildFailureMessage(code, objects, data);

        // Assert
        assertNotNull(response);
        assertEquals(code, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @Test
    void buildFailureMessage_WithNullObjects_ShouldReturnFailureMessage() {
        // Arrange
        String code = "ERROR_CODE";
        Object[] objects = null;
        Object data = new Object();
        String expectedDesc = "Error occurred";
        when(messageSource.getMessage(eq(code), eq(null), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildFailureMessage(code, objects, data);

        // Assert
        assertNotNull(response);
        assertEquals(code, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @Test
    void buildFailureMessage_WithNullData_ShouldReturnFailureMessage() {
        // Arrange
        String code = "ERROR_CODE";
        Object[] objects = new Object[]{"param1"};
        Object data = null;
        String expectedDesc = "Error occurred with param1";
        when(messageSource.getMessage(eq(code), eq(objects), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildFailureMessage(code, objects, data);

        // Assert
        assertNotNull(response);
        assertEquals(code, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertNull(response.getData());
    }

    @Test
    void buildFailureMessage_WhenMessageNotFound_ShouldReturnUndefinedMessage() {
        // Arrange
        String code = "ERROR_CODE";
        Object[] objects = new Object[]{"param1"};
        Object data = new Object();
        when(messageSource.getMessage(eq(code), eq(objects), eq(Constant.UNDEFINED), eq(locale)))
                .thenReturn(Constant.UNDEFINED);

        // Act
        RespMessage response = messageBuilder.buildFailureMessage(code, objects, data);

        // Assert
        assertNotNull(response);
        assertEquals(code, response.getRespCode());
        assertEquals(Constant.UNDEFINED, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @Test
    void buildFailureMessage_WithDifferentLocale_ShouldUseCorrectLocale() {
        // Arrange
        String code = "ERROR_CODE";
        Object[] objects = new Object[]{"param1"};
        Object data = new Object();
        Locale differentLocale = Locale.FRANCE;
        mockedLocaleContextHolder.when(LocaleContextHolder::getLocale).thenReturn(differentLocale);
        String expectedDesc = "Erreur survenue avec param1";
        when(messageSource.getMessage(eq(code), eq(objects), eq(Constant.UNDEFINED), eq(differentLocale)))
                .thenReturn(expectedDesc);

        // Act
        RespMessage response = messageBuilder.buildFailureMessage(code, objects, data);

        // Assert
        assertNotNull(response);
        assertEquals(code, response.getRespCode());
        assertEquals(expectedDesc, response.getRespDesc());
        assertEquals(data, response.getData());
    }

    @AfterEach
    void tearDown() {
        // Close static mocks after each test
        if (mockedLocaleContextHolder != null) {
            mockedLocaleContextHolder.close();
        }
    }
}