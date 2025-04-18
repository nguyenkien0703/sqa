package com.ptit.coffee_shop.repository;

import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.model.Conversation;
import com.ptit.coffee_shop.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String name, String email, Status status) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("dummy-password");
        user.setStatus(status);
        return user;
    }

    /**
     * ✅ TC1: Test findByUserIsActive() khi có cả host ACTIVE và INACTIVE.
     * ➤ Mong đợi: chỉ các conversation có host ACTIVE được trả về.
     */
    @Test
    @Transactional
    @DisplayName("findByUserIsActive() trả về chỉ các conversation có host ACTIVE")
    void test_findByUserIsActive_mixedStatus() {
        // Given
        User activeUser = createUser("Active User", "active@example.com", Status.ACTIVE);
        User inactiveUser = createUser("Inactive User", "inactive@example.com", Status.INACTIVE);

        userRepository.saveAll(List.of(activeUser, inactiveUser));

        Conversation c1 = new Conversation();
        c1.setHost(activeUser);

        Conversation c2 = new Conversation();
        c2.setHost(inactiveUser);

        conversationRepository.saveAll(List.of(c1, c2));

        // When
        List<Conversation> activeConversations = conversationRepository.findByUserIsActive();

        // Then
        assertEquals(1, activeConversations.size());
        assertEquals(Status.ACTIVE, activeConversations.get(0).getHost().getStatus());
    }

    /**
     * ✅ TC2: Test findByUserIsActive() khi tất cả host đều ACTIVE.
     * ➤ Mong đợi: trả về tất cả các conversation.
     */
    @Test
    @Transactional
    @DisplayName("findByUserIsActive() trả về toàn bộ conversation nếu tất cả host ACTIVE")
    void test_findByUserIsActive_allActive() {
        User u1 = createUser("A1", "a1@example.com", Status.ACTIVE);
        User u2 = createUser("A2", "a2@example.com", Status.ACTIVE);

        userRepository.saveAll(List.of(u1, u2));

        Conversation c1 = new Conversation();
        c1.setHost(u1);
        Conversation c2 = new Conversation();
        c2.setHost(u2);
        conversationRepository.saveAll(List.of(c1, c2));

        List<Conversation> result = conversationRepository.findByUserIsActive();
        assertEquals(2, result.size());
        result.forEach(c -> assertEquals(Status.ACTIVE, c.getHost().getStatus()));
    }

    /**
     * ✅ TC3: Test findByUserIsActive() khi tất cả host đều INACTIVE.
     * ➤ Mong đợi: danh sách trả về rỗng.
     */
    @Test
    @Transactional
    @DisplayName("findByUserIsActive() trả về rỗng nếu không có host ACTIVE")
    void test_findByUserIsActive_allInactive() {
        User u1 = createUser("I1", "i1@example.com", Status.INACTIVE);
        userRepository.save(u1);

        Conversation c1 = new Conversation();
        c1.setHost(u1);
        conversationRepository.save(c1);

        List<Conversation> result = conversationRepository.findByUserIsActive();
        assertTrue(result.isEmpty());
    }

    /**
     * ✅ TC4: Test findByUserIsActive() khi không có conversation nào.
     * ➤ Mong đợi: danh sách trả về rỗng.
     */
    @Test
    @Transactional
    @DisplayName("findByUserIsActive() trả về rỗng khi không có conversation nào")
    void test_findByUserIsActive_emptyDB() {
        List<Conversation> result = conversationRepository.findByUserIsActive();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
