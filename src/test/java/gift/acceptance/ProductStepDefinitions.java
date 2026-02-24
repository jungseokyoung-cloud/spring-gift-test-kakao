package gift.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceTestContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResponseEntity<String> listResponse;

    @When("{string}에 {int}원짜리 {string}을 생성한다")
    public void 상품을_생성한다(String categoryName, int price, String productName) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", productName);
        params.add("price", String.valueOf(price));
        params.add("imageUrl", "http://image.url");
        params.add("categoryId", String.valueOf(context.getCategoryId(categoryName)));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products", params, String.class
        );
        context.setResponse(response);

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode body = objectMapper.readTree(response.getBody());
            context.putProductId(productName, body.get("id").asLong());
        }
    }

    @Given("{string}에 {int}원짜리 {string}가 생성되어 있다")
    public void 상품이_생성되어_있다(String categoryName, int price, String productName) throws Exception {
        상품을_생성한다(categoryName, price, productName);
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(200);
    }

    @When("존재하지 않는 카테고리에 {int}원짜리 {string}을 생성한다")
    public void 존재하지_않는_카테고리에_상품_생성(int price, String productName) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", productName);
        params.add("price", String.valueOf(price));
        params.add("imageUrl", "http://image.url");
        params.add("categoryId", "99999");

        context.setResponse(restTemplate.postForEntity(
                "/api/products", params, String.class
        ));
    }

    @When("상품 목록을 조회한다")
    public void 상품_목록을_조회한다() {
        listResponse = restTemplate.getForEntity("/api/products", String.class);
        context.setResponse(listResponse);
    }

    @And("상품 목록에 {string}이 포함되어 있다")
    public void 상품_목록에_포함되어_있다(String productName) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/products", String.class);
        JsonNode products = objectMapper.readTree(response.getBody());

        boolean found = false;
        for (JsonNode product : products) {
            if (productName.equals(product.get("name").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @And("상품 목록의 크기는 {int}이다")
    public void 상품_목록의_크기_검증(int expectedSize) throws Exception {
        JsonNode products = objectMapper.readTree(listResponse.getBody());
        assertThat(products.size()).isEqualTo(expectedSize);
    }
}
