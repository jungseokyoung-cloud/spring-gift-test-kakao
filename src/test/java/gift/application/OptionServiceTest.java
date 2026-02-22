package gift.application;

import gift.model.Category;
import gift.model.CategoryRepository;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OptionServiceTest {

    @Autowired
    private OptionService optionService;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        optionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(new Category("테스트 카테고리"));
        product = productRepository.save(new Product("테스트 상품", 10000, "http://image.url", category));
    }

    @Nested
    @DisplayName("create: 옵션 생성")
    class Create {

        @Test
        void 옵션_생성_성공() throws Exception {
            // given
            CreateOptionRequest request = createRequest("기본 옵션", 10, product.getId());

            // when
            Option option = optionService.create(request);

            // then
            assertThat(option.getId()).isNotNull();
            assertThat(option.getName()).isEqualTo("기본 옵션");
            assertThat(option.getQuantity()).isEqualTo(10);
            assertThat(option.getProduct().getId()).isEqualTo(product.getId());
        }

        @Test
        void 존재하지_않는_상품이면_예외가_발생한다() throws Exception {
            // given
            CreateOptionRequest request = createRequest("기본 옵션", 10, 99999L);

            // when & then
            assertThatThrownBy(() -> optionService.create(request))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void 생성된_옵션을_조회할_수_있다() throws Exception {
            // given
            CreateOptionRequest request = createRequest("기본 옵션", 10, product.getId());
            optionService.create(request);

            // when
            List<Option> options = optionService.retrieve();

            // then
            assertThat(options).hasSize(1);
            assertThat(options.get(0).getName()).isEqualTo("기본 옵션");
        }
    }

    @Nested
    @DisplayName("retrieve: 옵션 조회")
    class Retrieve {

        @Test
        void 데이터가_없으면_빈_리스트를_반환한다() {
            // when
            List<Option> options = optionService.retrieve();

            // then
            assertThat(options).isEmpty();
        }

        @Test
        void 여러_옵션을_조회할_수_있다() throws Exception {
            // given
            optionService.create(createRequest("옵션1", 10, product.getId()));
            optionService.create(createRequest("옵션2", 20, product.getId()));

            // when
            List<Option> options = optionService.retrieve();

            // then
            assertThat(options).hasSize(2);
        }
    }

    private CreateOptionRequest createRequest(String name, int quantity, Long productId) throws Exception {
        CreateOptionRequest request = new CreateOptionRequest();
        setField(request, "name", name);
        setField(request, "quantity", quantity);
        setField(request, "productId", productId);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
