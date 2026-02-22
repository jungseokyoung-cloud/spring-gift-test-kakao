package gift.application;

import gift.model.Category;
import gift.model.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Nested
    @DisplayName("create: 카테고리 생성")
    class Create {

        @Test
        void 카테고리_생성_성공() throws Exception {
            // given
            CreateCategoryRequest request = createRequest("테스트 카테고리");

            // when
            Category category = categoryService.create(request);

            // then
            assertThat(category.getId()).isNotNull();
            assertThat(category.getName()).isEqualTo("테스트 카테고리");
        }

        @Test
        void 생성된_카테고리를_조회할_수_있다() throws Exception {
            // given
            CreateCategoryRequest request = createRequest("테스트 카테고리");
            categoryService.create(request);

            // when
            List<Category> categories = categoryService.retrieve();

            // then
            assertThat(categories).hasSize(1);
            assertThat(categories.get(0).getName()).isEqualTo("테스트 카테고리");
        }

        @Test
        void 여러_카테고리를_생성하고_조회할_수_있다() throws Exception {
            // given
            categoryService.create(createRequest("카테고리1"));
            categoryService.create(createRequest("카테고리2"));
            categoryService.create(createRequest("카테고리3"));

            // when
            List<Category> categories = categoryService.retrieve();

            // then
            assertThat(categories).hasSize(3);
        }
    }

    @Nested
    @DisplayName("retrieve: 카테고리 조회")
    class Retrieve {

        @Test
        void 데이터가_없으면_빈_리스트를_반환한다() {
            // when
            List<Category> categories = categoryService.retrieve();

            // then
            assertThat(categories).isEmpty();
        }
    }

    private CreateCategoryRequest createRequest(String name) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        setField(request, "name", name);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
