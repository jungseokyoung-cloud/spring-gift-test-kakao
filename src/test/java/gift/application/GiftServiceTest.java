package gift.application;

import gift.model.Category;
import gift.model.CategoryRepository;
import gift.model.Member;
import gift.model.MemberRepository;
import gift.model.Option;
import gift.model.OptionRepository;
import gift.model.Product;
import gift.model.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class GiftServiceTest {

    @Autowired
    private GiftService giftService;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member sender;
    private Member receiver;
    private Option option;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category("테스트 카테고리"));
        Product product = productRepository.save(new Product("테스트 상품", 10000, "http://image.url", category));
        option = optionRepository.save(new Option("기본 옵션", 10, product));
        sender = memberRepository.save(new Member("보내는사람", "sender@test.com"));
        receiver = memberRepository.save(new Member("받는사람", "receiver@test.com"));
    }

    private GiveGiftRequest createRequest(Long optionId, int quantity, Long receiverId, String message) throws Exception {
        GiveGiftRequest request = new GiveGiftRequest();
        setField(request, "optionId", optionId);
        setField(request, "quantity", quantity);
        setField(request, "receiverId", receiverId);
        setField(request, "message", message);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("give: 선물 전송")
    class Give {

        @Test
        void 선물_전송_성공_시_재고가_차감된다() throws Exception {
            // given
            GiveGiftRequest request = createRequest(option.getId(), 3, receiver.getId(), "선물입니다");

            // when
            giftService.give(request, sender.getId());

            // then
            Option found = optionRepository.findById(option.getId()).orElseThrow();
            assertThat(found.getQuantity()).isEqualTo(7);
        }

        @Test
        void 존재하지_않는_옵션으로_전송하면_예외가_발생한다() throws Exception {
            // given
            GiveGiftRequest request = createRequest(999L, 1, receiver.getId(), "선물입니다");

            // when & then
            assertThatThrownBy(() -> giftService.give(request, sender.getId()))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void 재고_부족_시_예외가_발생한다() throws Exception {
            // given
            GiveGiftRequest request = createRequest(option.getId(), 11, receiver.getId(), "선물입니다");

            // when & then
            assertThatThrownBy(() -> giftService.give(request, sender.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 재고_부족_시_재고가_변경되지_않는다() throws Exception {
            // given
            GiveGiftRequest request = createRequest(option.getId(), 11, receiver.getId(), "선물입니다");

            // when
            try {
                giftService.give(request, sender.getId());
            } catch (IllegalStateException ignored) {
            }

            // then
            Option found = optionRepository.findById(option.getId()).orElseThrow();
            assertThat(found.getQuantity()).isEqualTo(10);
        }
    }
}
