package gift.application;

import gift.model.Category;
import gift.model.CategoryRepository;
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
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(new Category("테스트 카테고리"));
    }

    @Nested
    @DisplayName("create: 상품 생성")
    class Create {

        @Test
        void 상품_생성_성공() throws Exception {
            // given
            CreateProductRequest request = createRequest("테스트 상품", 10000, "http://image.url", category.getId());

            // when
            Product product = productService.create(request);

            // then
            assertThat(product.getId()).isNotNull();
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getPrice()).isEqualTo(10000);
            assertThat(product.getCategory().getId()).isEqualTo(category.getId());
        }

        @Test
        void 존재하지_않는_카테고리면_예외가_발생한다() throws Exception {
            // given
            CreateProductRequest request = createRequest("테스트 상품", 10000, "http://image.url", 99999L);

            // when & then
            assertThatThrownBy(() -> productService.create(request))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void 생성된_상품을_조회할_수_있다() throws Exception {
            // given
            CreateProductRequest request = createRequest("테스트 상품", 10000, "http://image.url", category.getId());
            productService.create(request);

            // when
            List<Product> products = productService.retrieve();

            // then
            assertThat(products).hasSize(1);
            assertThat(products.get(0).getName()).isEqualTo("테스트 상품");
        }
    }

    @Nested
    @DisplayName("retrieve: 상품 조회")
    class Retrieve {

        @Test
        void 데이터가_없으면_빈_리스트를_반환한다() {
            // when
            List<Product> products = productService.retrieve();

            // then
            assertThat(products).isEmpty();
        }

        @Test
        void 여러_상품을_조회할_수_있다() throws Exception {
            // given
            productService.create(createRequest("상품1", 10000, "http://image1.url", category.getId()));
            productService.create(createRequest("상품2", 20000, "http://image2.url", category.getId()));

            // when
            List<Product> products = productService.retrieve();

            // then
            assertThat(products).hasSize(2);
        }
    }

    private CreateProductRequest createRequest(String name, int price, String imageUrl, Long categoryId) throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        setField(request, "name", name);
        setField(request, "price", price);
        setField(request, "imageUrl", imageUrl);
        setField(request, "categoryId", categoryId);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
