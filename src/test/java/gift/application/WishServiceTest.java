package gift.application;

import gift.model.Category;
import gift.model.CategoryRepository;
import gift.model.Member;
import gift.model.MemberRepository;
import gift.model.Product;
import gift.model.ProductRepository;
import gift.model.Wish;
import gift.model.WishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class WishServiceTest {

    @Autowired
    private WishService wishService;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        wishRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(new Member("테스트 회원", "test@test.com"));
        Category category = categoryRepository.save(new Category("테스트 카테고리"));
        product = productRepository.save(new Product("테스트 상품", 10000, "http://image.url", category));
    }

    @Nested
    @DisplayName("create: 위시 생성")
    class Create {

        @Test
        void 위시_생성_성공() throws Exception {
            // given
            CreateWishRequest request = createRequest(product.getId());

            // when
            Wish wish = wishService.create(member.getId(), request);

            // then
            assertThat(wish.getId()).isNotNull();
            assertThat(wish.getMember().getId()).isEqualTo(member.getId());
            assertThat(wish.getProduct().getId()).isEqualTo(product.getId());
        }

        @Test
        void 존재하지_않는_회원이면_예외가_발생한다() throws Exception {
            // given
            CreateWishRequest request = createRequest(product.getId());

            // when & then
            assertThatThrownBy(() -> wishService.create(99999L, request))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void 존재하지_않는_상품이면_예외가_발생한다() throws Exception {
            // given
            CreateWishRequest request = createRequest(99999L);

            // when & then
            assertThatThrownBy(() -> wishService.create(member.getId(), request))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    private CreateWishRequest createRequest(Long productId) throws Exception {
        CreateWishRequest request = new CreateWishRequest();
        setField(request, "productId", productId);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
