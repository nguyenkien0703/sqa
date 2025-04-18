package com.ptit.coffee_shop.service;

import com.ptit.coffee_shop.common.Constant;
import com.ptit.coffee_shop.common.enums.RoleEnum;
import com.ptit.coffee_shop.common.enums.Status;
import com.ptit.coffee_shop.config.MessageBuilder;
import com.ptit.coffee_shop.exception.CoffeeShopException;
import com.ptit.coffee_shop.model.Role;
import com.ptit.coffee_shop.model.ShippingAddress;
import com.ptit.coffee_shop.model.User;
import com.ptit.coffee_shop.payload.request.ShippingAddressRequest;
import com.ptit.coffee_shop.payload.response.RespMessage;
import com.ptit.coffee_shop.payload.response.ShippingAddressResponse;
import com.ptit.coffee_shop.repository.RoleRepository;
import com.ptit.coffee_shop.repository.ShippingAddressRepository;
import com.ptit.coffee_shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ShippingAddressServiceTest {

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    private ShippingAddressService shippingAddressService;

    private User testUser;
    private Role userRole;





    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(0, RoleEnum.ROLE_USER)));

        testUser = User.builder()
                .email("testuser@example.com")
                .password(passwordEncoder.encode("password"))
                .role(userRole)
                .status(Status.ACTIVE)
                .created_at(new Date())
                .updated_at(new Date())
                .name("Test User")
                .phone("123456789")
                .profile_img("profile_img_url")
                .build();

        userRepository.save(testUser);

        shippingAddressService = new ShippingAddressService(
                shippingAddressRepository,
                userRepository,
                new MessageBuilder(messageSource)
        );
    }

    /**
     * TC001 - Method: addShippingAddress
     * Description: Thêm địa chỉ giao hàng thành công cho user hợp lệ
     * Input: User tồn tại, tên người nhận, SĐT, địa chỉ
     * Expected output: RespMessage có respCode = SUCCESS
     */
    @Test
    void testAddShippingAddress_Success() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId(testUser.getId());
        request.setReceiverName("John Doe");
        request.setReceiverPhone("123456789");
        request.setLocation("Some Location");

        RespMessage response = shippingAddressService.addShippingAddress(request);
        assertEquals(Constant.SUCCESS, response.getRespCode());
    }

    /**
     * TC002 - Method: addShippingAddress
     * Description: Thêm địa chỉ giao hàng cho user không tồn tại
     * Input: UserId không tồn tại
     * Expected output: CoffeeShopException được ném ra
     */
    @Test
    void testAddShippingAddress_UserNotFound() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId(999L);
        request.setReceiverName("John Doe");
        request.setReceiverPhone("123456789");
        request.setLocation("Some Location");

        assertThrows(CoffeeShopException.class, () -> {
            shippingAddressService.addShippingAddress(request);
        });
    }

    /**
     * TC003 - Method: updateShippingAddress
     * Description: Cập nhật thông tin địa chỉ thành công
     * Input: ShippingAddress đã tồn tại, cập nhật thông tin
     * Expected output: RespMessage có respCode = SUCCESS
     */
    @Test
    void testUpdateShippingAddress_Success() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId(testUser.getId());
        request.setReceiverName("John Doe");
        request.setReceiverPhone("123456789");
        request.setLocation("Location A");

        RespMessage addResponse = shippingAddressService.addShippingAddress(request);
        ShippingAddressResponse added = (ShippingAddressResponse) addResponse.getData();

        request.setId(added.getId());
        request.setReceiverName("Updated Name");
        request.setReceiverPhone("000000000");
        request.setLocation("Location B");
        request.setStatus(Status.ACTIVE);

        RespMessage updateResponse = shippingAddressService.updateShippingAddress(request);
        assertEquals(Constant.SUCCESS, updateResponse.getRespCode());
    }

    /**
     * TC004 - Method: updateShippingAddress
     * Description: Cập nhật địa chỉ không tồn tại
     * Input: ID không tồn tại
     * Expected output: CoffeeShopException được ném ra
     */
    @Test
    void testUpdateShippingAddress_NotFound() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId(testUser.getId());
        request.setId(999L);
        request.setReceiverName("Jane Doe");
        request.setReceiverPhone("987654321");
        request.setLocation("New Location");

        assertThrows(CoffeeShopException.class, () -> {
            shippingAddressService.updateShippingAddress(request);
        });
    }

    /**
     * TC005 - Method: deleteShippingAddress
     * Description: Xóa địa chỉ giao hàng thành công
     * Input: ShippingAddress hợp lệ
     * Expected output: RespMessage có respCode = SUCCESS
     */
    @Test
    void testDeleteShippingAddress_Success() {
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId(testUser.getId());
        request.setReceiverName("Receiver");
        request.setReceiverPhone("111222333");
        request.setLocation("Delete Street");

        RespMessage addResp = shippingAddressService.addShippingAddress(request);
        ShippingAddressResponse address = (ShippingAddressResponse) addResp.getData();

        RespMessage deleteResp = shippingAddressService.deleteShippingAddress(address.getId());
        assertEquals(Constant.SUCCESS, deleteResp.getRespCode());
    }

    /**
     * TC006 - Method: deleteShippingAddress
     * Description: Xóa địa chỉ không tồn tại
     * Input: ID không tồn tại
     * Expected output: CoffeeShopException được ném ra
     */
    @Test
    void testDeleteShippingAddress_NotFound() {
        assertThrows(CoffeeShopException.class, () -> {
            shippingAddressService.deleteShippingAddress(999L);
        });
    }

    /**
     * TC007 - Method: getShippingAddress
     * Description: Lấy địa chỉ giao hàng khi user không tồn tại
     * Input: Email không tồn tại trong security context
     * Expected output: CoffeeShopException được ném ra
     */
    @Test
    void testGetShippingAddress_UserNotFound() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("nonexist@example.com", null, Collections.emptyList())
        );
        assertThrows(CoffeeShopException.class, () -> {
            shippingAddressService.getShippingAddress();
        });
    }

    /**
     * TC008 - Method: getShippingAddress
     * Description: Lấy địa chỉ khi danh sách địa chỉ rỗng
     * Input: User hợp lệ, không có ShippingAddress
     * Expected output: RespMessage có respCode = SUCCESS và data = ""
     */
    @Test
    void testGetShippingAddress_EmptyList() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, Collections.emptyList())
        );
        RespMessage response = shippingAddressService.getShippingAddress();
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertEquals("", response.getData());
    }

    /**
     * TC009 - Method: getShippingAddress
     * Description: Có địa chỉ nhưng tất cả đều INACTIVE
     * Input: ShippingAddress có status = INACTIVE
     * Expected output: RespMessage trả về danh sách rỗng
     */
    @Test
    void testGetShippingAddress_NoActiveAddress() {
        ShippingAddress address = new ShippingAddress();
        address.setUser(testUser);
        address.setReceiverName("Inactive");
        address.setReceiverPhone("000");
        address.setLocation("Nowhere");
        address.setStatus(Status.INACTIVE);
        shippingAddressRepository.save(address);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, Collections.emptyList())
        );
        RespMessage response = shippingAddressService.getShippingAddress();
        assertEquals(Constant.SUCCESS, response.getRespCode());
        assertEquals(Collections.emptyList(), response.getData());
    }

    /**
     * TC010 - Method: getShippingAddress
     * Description: Lấy danh sách địa chỉ ACTIVE
     * Input: ShippingAddress có status = ACTIVE
     * Expected output: Danh sách có 1 địa chỉ với tên người nhận đúng
     */
    @Test
    void testGetShippingAddress_WithActiveAddress() {
        ShippingAddress address = new ShippingAddress();
        address.setUser(testUser);
        address.setReceiverName("Active");
        address.setReceiverPhone("111");
        address.setLocation("Somewhere");
        address.setStatus(Status.ACTIVE);
        shippingAddressRepository.save(address);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, Collections.emptyList())
        );
        RespMessage response = shippingAddressService.getShippingAddress();
        assertEquals(Constant.SUCCESS, response.getRespCode());
        List<ShippingAddressResponse> list = (List<ShippingAddressResponse>) response.getData();
        assertEquals(1, list.size());
        assertEquals("Active", list.get(0).getReceiverName());
    }
}
