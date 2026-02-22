package gift.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("테스트 카테고리");
        product = new Product("테스트 상품", 10000, "http://image.url", category);
    }

    @Nested
    @DisplayName("decrease: 재고 차감")
    class Decrease {

        @Test
        void 재고가_충분하면_정상_차감된다() {
            // given
            Option option = new Option("기본 옵션", 10, product);

            // when
            option.decrease(3);

            // then
            assertThat(option.getQuantity()).isEqualTo(7);
        }

        @Test
        void 재고가_부족하면_예외가_발생한다() {
            // given
            Option option = new Option("기본 옵션", 2, product);

            // when & then
            assertThatThrownBy(() -> option.decrease(5))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 재고와_요청량이_같으면_재고가_0이_된다() {
            // given
            Option option = new Option("기본 옵션", 5, product);

            // when
            option.decrease(5);

            // then
            assertThat(option.getQuantity()).isZero();
        }

        @Test
        void 여러_번_차감해도_정상_동작한다() {
            // given
            Option option = new Option("기본 옵션", 10, product);

            // when
            option.decrease(3);
            option.decrease(4);

            // then
            assertThat(option.getQuantity()).isEqualTo(3);
        }

        @Test
        void 영개_차감_시_재고가_변하지_않는다() {
            // given
            Option option = new Option("기본 옵션", 10, product);

            // when
            option.decrease(0);

            // then
            assertThat(option.getQuantity()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("decrease: 재고 부족 예외")
    class DecreaseInsufficientStock {

        @Test
        void 재고가_0일_때_양수_차감_시_예외가_발생한다() {
            // given
            Option option = new Option("기본 옵션", 0, product);

            // when & then
            assertThatThrownBy(() -> option.decrease(1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 재고보다_1개_많은_수량_요청_시_예외가_발생한다() {
            // given
            Option option = new Option("기본 옵션", 5, product);

            // when & then
            assertThatThrownBy(() -> option.decrease(6))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 예외_발생_후_재고가_변경되지_않는다() {
            // given
            Option option = new Option("기본 옵션", 3, product);

            // when & then
            assertThatThrownBy(() -> option.decrease(5))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(option.getQuantity()).isEqualTo(3);
        }

        @Test
        void 여러_번_차감_후_남은_재고보다_많이_요청하면_예외가_발생한다() {
            // given
            Option option = new Option("기본 옵션", 10, product);
            option.decrease(7);

            // when & then
            assertThatThrownBy(() -> option.decrease(4))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
