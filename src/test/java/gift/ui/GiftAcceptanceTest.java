package gift.ui;

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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GiftAcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Category category;
    private Product product;
    private Option option;
    private Member sender;
    private Member receiver;

    @BeforeEach
    void setUp() {
        optionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        category = categoryRepository.save(new Category("테스트 카테고리"));
        product = productRepository.save(new Product("테스트 상품", 10000, "http://image.url", category));
        option = optionRepository.save(new Option("기본 옵션", 10, product));
        sender = memberRepository.save(new Member("보내는사람", "sender@test.com"));
        receiver = memberRepository.save(new Member("받는사람", "receiver@test.com"));
    }

    @Nested
    @DisplayName("POST /api/gifts: 선물 전송 API")
    class GiveGift {

        @Test
        void 선물_전송_API_성공() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Member-Id", sender.getId().toString());

            String requestBody = """
                {
                    "optionId": %d,
                    "quantity": 3,
                    "receiverId": %d,
                    "message": "축하해"
                }
                """.formatted(option.getId(), receiver.getId());

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/gifts",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 재고 감소 확인 (다음 행동 검증)
            Option updated = optionRepository.findById(option.getId()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualTo(7);
        }

        @Test
        void Member_Id_헤더가_없으면_400_에러() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Member-Id 헤더 없음

            String requestBody = """
                {
                    "optionId": %d,
                    "quantity": 1,
                    "receiverId": %d,
                    "message": "축하해"
                }
                """.formatted(option.getId(), receiver.getId());

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/gifts",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 존재하지_않는_옵션이면_500_에러() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Member-Id", sender.getId().toString());

            String requestBody = """
                {
                    "optionId": 99999,
                    "quantity": 1,
                    "receiverId": %d,
                    "message": "축하해"
                }
                """.formatted(receiver.getId());

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/gifts",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        void 재고가_부족하면_500_에러() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Member-Id", sender.getId().toString());

            String requestBody = """
                {
                    "optionId": %d,
                    "quantity": 100,
                    "receiverId": %d,
                    "message": "축하해"
                }
                """.formatted(option.getId(), receiver.getId());

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/gifts",
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

            // 재고 변경 없음 확인
            Option updated = optionRepository.findById(option.getId()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualTo(10);
        }
    }
}
