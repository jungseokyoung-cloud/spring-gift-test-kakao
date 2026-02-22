package gift.infrastructure;

import gift.model.Category;
import gift.model.CategoryRepository;
import gift.model.Gift;
import gift.model.GiftDelivery;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FakeGiftDeliveryTest {

    @Autowired
    private GiftDelivery giftDelivery;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member sender;
    private Member receiver;
    private Option option;

    @BeforeEach
    void setUp() {
        optionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        sender = memberRepository.save(new Member("보내는사람", "sender@test.com"));
        receiver = memberRepository.save(new Member("받는사람", "receiver@test.com"));

        Category category = categoryRepository.save(new Category("테스트 카테고리"));
        Product product = productRepository.save(new Product("테스트 상품", 10000, "http://image.url", category));
        option = optionRepository.save(new Option("기본 옵션", 10, product));
    }

    @Nested
    @DisplayName("deliver: 선물 전달")
    class Deliver {

        @Test
        void 선물_전달_성공() {
            // given
            Gift gift = new Gift(sender.getId(), receiver.getId(), option, 1, "축하해");

            // when & then
            assertThatCode(() -> giftDelivery.deliver(gift))
                    .doesNotThrowAnyException();
        }

        @Test
        void 존재하지_않는_회원이면_예외가_발생한다() {
            // given
            Gift gift = new Gift(99999L, receiver.getId(), option, 1, "축하해");

            // when & then
            assertThatThrownBy(() -> giftDelivery.deliver(gift))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}
