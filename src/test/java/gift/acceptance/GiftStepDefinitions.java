package gift.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.model.Member;
import gift.model.MemberRepository;
import gift.model.Option;
import gift.model.OptionRepository;
import gift.model.Product;
import gift.model.ProductRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class GiftStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AcceptanceTestContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Given("{string}에 {int}원짜리 {string}이 있다")
    public void 카테고리에_상품이_있다(String categoryName, int price, String productName) throws Exception {
        // HTTP: 카테고리 생성 (POST /api/categories)
        MultiValueMap<String, String> categoryParams = new LinkedMultiValueMap<>();
        categoryParams.add("name", categoryName);

        ResponseEntity<String> categoryResponse = restTemplate.postForEntity(
                "/api/categories", categoryParams, String.class
        );
        assertThat(categoryResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode categoryBody = objectMapper.readTree(categoryResponse.getBody());
        Long categoryId = categoryBody.get("id").asLong();
        context.putCategoryId(categoryName, categoryId);

        // HTTP: 상품 생성 (POST /api/products)
        MultiValueMap<String, String> productParams = new LinkedMultiValueMap<>();
        productParams.add("name", productName);
        productParams.add("price", String.valueOf(price));
        productParams.add("imageUrl", "http://image.url");
        productParams.add("categoryId", String.valueOf(categoryId));

        ResponseEntity<String> productResponse = restTemplate.postForEntity(
                "/api/products", productParams, String.class
        );
        assertThat(productResponse.getStatusCode().value()).isEqualTo(200);
        JsonNode productBody = objectMapper.readTree(productResponse.getBody());
        Long productId = productBody.get("id").asLong();
        context.putProductId(productName, productId);
    }

    @And("{string}에 재고 {int}개인 {string}이 있다")
    public void 상품에_옵션이_있다(String productName, int quantity, String optionName) {
        // 옵션 API 없음 - Repository 사용
        Product product = productRepository.findById(context.getProductId(productName)).orElseThrow();
        Option option = optionRepository.save(new Option(optionName, quantity, product));
        context.putOptionId(optionName, option.getId());
    }

    @And("{string}과 {string} 회원이 있다")
    public void 회원이_있다(String senderName, String receiverName) {
        // 회원 API 없음 - Repository 사용
        Member sender = memberRepository.save(new Member(senderName, senderName + "@test.com"));
        context.putMemberId(senderName, sender.getId());
        Member receiver = memberRepository.save(new Member(receiverName, receiverName + "@test.com"));
        context.putMemberId(receiverName, receiver.getId());
    }

    @When("{string}이 {string}에게 {string} {int}개를 선물한다")
    public void 선물한다(String senderName, String receiverName, String optionName, int quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Member-Id", context.getMemberId(senderName).toString());

        String requestBody = """
                {
                    "optionId": %d,
                    "quantity": %d,
                    "receiverId": %d,
                    "message": "선물입니다"
                }
                """.formatted(context.getOptionId(optionName), quantity, context.getMemberId(receiverName));

        context.setResponse(restTemplate.exchange(
                "/api/gifts",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Void.class
        ));
    }

    @When("{string}이 {string}에게 존재하지 않는 옵션을 선물한다")
    public void 존재하지_않는_옵션을_선물한다(String senderName, String receiverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Member-Id", context.getMemberId(senderName).toString());

        String requestBody = """
                {
                    "optionId": 99999,
                    "quantity": 1,
                    "receiverId": %d,
                    "message": "선물입니다"
                }
                """.formatted(context.getMemberId(receiverName));

        context.setResponse(restTemplate.exchange(
                "/api/gifts",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Void.class
        ));
    }

    @When("회원 정보 없이 {string}에게 {string} {int}개를 선물한다")
    public void 회원_정보_없이_선물한다(String receiverName, String optionName, int quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = """
                {
                    "optionId": %d,
                    "quantity": %d,
                    "receiverId": %d,
                    "message": "선물입니다"
                }
                """.formatted(context.getOptionId(optionName), quantity, context.getMemberId(receiverName));

        context.setResponse(restTemplate.exchange(
                "/api/gifts",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Void.class
        ));
    }

    @And("{string}의 재고는 {int}개이다")
    public void 재고_검증(String optionName, int expectedQuantity) {
        // 옵션 조회 API 없음 - Repository 사용
        Option found = optionRepository.findById(context.getOptionId(optionName)).orElseThrow();
        assertThat(found.getQuantity()).isEqualTo(expectedQuantity);
    }
}
