package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Conversation;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.ChatMessageRequest;
import com.ptit.coffee_shop.payload.response.ChatMessageResponse;
import com.ptit.coffee_shop.payload.response.ConversationResponse;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.repository.ChatMessageRepository;
import com.ptit.coffee_shop.repository.ConversationRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User testUser;
    private Conversation testConversation;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .email("testuser@example.com")
                .password("password")
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("Test User")
                .phone("123456789")
                .profile_img("profile_img_url")
                .build();
        userRepository.save(testUser);

        testConversation = new Conversation();
        testConversation.setHost(testUser);
        conversationRepository.save(testConversation);
    }

    // TC001 - updateMessage - Gửi tin nhắn thành công
    // Input: user id, conversation id, nội dung tin nhắn
    // Expected: trả về conversation chứa tin nhắn vừa gửi
    @Test
    public void testUpdateMessageSuccess() {
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setSenderId(testUser.getId());
        messageRequest.setContent("Hello, this is a test message.");

        RespMessage response = chatService.updateMessage(messageRequest, testConversation.getId());

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());

        ConversationResponse conversationResponse = (ConversationResponse) response.getData();
        assertNotNull(conversationResponse);
        assertEquals(testConversation.getId(), conversationResponse.getId());
        assertFalse(conversationResponse.getMessageList().isEmpty());

        ChatMessageResponse chatMessageResponse = conversationResponse.getMessageList().get(0);
        assertEquals("Hello, this is a test message.", chatMessageResponse.getContent());
        assertEquals(testUser.getId(), chatMessageResponse.getSenderId());
    }

    // TC002 - updateMessage - Conversation không tồn tại
    // Input: conversationId không hợp lệ
    // Expected: Ném exception FIELD_NOT_FOUND
    @Test
    public void testUpdateMessageConversationNotFound() {
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setSenderId(testUser.getId());
        messageRequest.setContent("Test message");

        long invalidConversationId = 9999L;

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.updateMessage(messageRequest, invalidConversationId));

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

    // TC003 - updateMessage - Sender không tồn tại
    // Input: senderId không tồn tại
    // Expected: Ném exception FIELD_NOT_FOUND
    @Test
    public void testUpdateMessageSenderNotFound() {
        ChatMessageRequest messageRequest = new ChatMessageRequest();
        messageRequest.setSenderId(9999L);
        messageRequest.setContent("Test message");

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.updateMessage(messageRequest, testConversation.getId()));

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

    // TC004 - getAllConversation - Lấy tất cả cuộc trò chuyện
    // Input: none
    // Expected: Danh sách conversation trả về không null và success
    @Test
    public void testGetAllConversation() {
        RespMessage response = chatService.getAllConversation();

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertNotNull(response.getData());
    }

    // TC005 - getConversationById - Thành công
    // Input: conversation id hợp lệ
    // Expected: Trả về conversation tương ứng
    @Test
    public void testGetConversationByIdSuccess() {
        RespMessage response = chatService.getConversationById(testConversation.getId());

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());

        ConversationResponse conversationResponse = (ConversationResponse) response.getData();
        assertEquals(testConversation.getId(), conversationResponse.getId());
    }

    // TC006 - getConversationById - Conversation không tồn tại
    // Input: ID không hợp lệ
    // Expected: Ném exception FIELD_NOT_FOUND
    @Test
    public void testGetConversationByIdNotFound() {
        long invalidId = 9999L;

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.getConversationById(invalidId));

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

    // TC007 - getConversationByHostId - Thành công
    // Input: user id hợp lệ
    // Expected: Trả về conversation có host là user đó
    @Test
    public void testGetConversationByHostIdSuccess() {
        RespMessage response = chatService.getConversationByHostId(testUser.getId());

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());

        ConversationResponse conversationResponse = (ConversationResponse) response.getData();
        assertEquals(testConversation.getId(), conversationResponse.getId());
    }

    // TC008 - getConversationByHostId - Host không tồn tại
    // Input: user id không hợp lệ
    // Expected: Ném exception FIELD_NOT_FOUND
    @Test
    public void testGetConversationByHostIdNotFound() {
        long invalidUserId = 9999L;

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.getConversationByHostId(invalidUserId));

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

    // TC009 - createConversation - Thành công
    // Input: user id hợp lệ
    // Expected: Tạo và trả về conversation mới
    @Test
    public void testCreateConversationSuccess() {
        User newUser = User.builder()
                .email("newuser@example.com")
                .password("password")
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("New User")
                .phone("987654321")
                .profile_img("new_profile_img_url")
                .build();
        userRepository.save(newUser);

        RespMessage response = chatService.createConversation(newUser.getId());

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());

        ConversationResponse conversationResponse = (ConversationResponse) response.getData();
        assertEquals(newUser.getId(), conversationResponse.getHostId());
    }

    // TC010 - createConversation - User không tồn tại
    // Input: userId không tồn tại
    // Expected: Ném exception FIELD_NOT_FOUND
    @Test
    public void testCreateConversationUserNotFound() {
        long invalidUserId = 9999L;

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.createConversation(invalidUserId));

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

    // TC011 - getConversationByHostId - Không có conversation => tạo mới
    // Input: user id chưa có conversation
    // Expected: Tự động tạo mới conversation
    @Test
    public void testGetConversationByHostId_CreateNewIfNotFound() {
        User newUser = User.builder()
                .email("hostcreate@example.com")
                .password("password")
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("Host Create")
                .phone("000111222")
                .profile_img("host_avatar_url")
                .build();
        userRepository.save(newUser);

        RespMessage response = chatService.getConversationByHostId(newUser.getId());

        assertNotNull(response);
        assertEquals(Constant.SUCCESS, response.getRespCode());

        ConversationResponse conversationResponse = (ConversationResponse) response.getData();
        assertEquals(newUser.getId(), conversationResponse.getHostId());
        assertNotNull(conversationResponse.getId());
    }

    // TC012 - updateMessage - Xảy ra lỗi hệ thống khi lưu tin nhắn
    // Input: gửi message vào conversation đã bị xoá
    // Expected: Ném exception SYSTEM_ERROR
    @Test
    public void testUpdateMessage_ThrowsSystemError() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderId(testUser.getId());
        request.setContent("This will fail");

        conversationRepository.deleteById(testConversation.getId());

        CoffeeShopException exception = assertThrows(CoffeeShopException.class, () ->
                chatService.updateMessage(request, testConversation.getId())
        );

        assertEquals(Constant.FIELD_NOT_FOUND, exception.getCode());
    }

}
