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

public class CategoryStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AcceptanceTestContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResponseEntity<String> listResponse;

    @When("{string} 카테고리를 생성한다")
    public void 카테고리를_생성한다(String categoryName) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", categoryName);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/categories", params, String.class
        );
        context.setResponse(response);

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode body = objectMapper.readTree(response.getBody());
            context.putCategoryId(categoryName, body.get("id").asLong());
        }
    }

    @Given("{string} 카테고리가 생성되어 있다")
    public void 카테고리가_생성되어_있다(String categoryName) throws Exception {
        카테고리를_생성한다(categoryName);
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(200);
    }

    @Given("{string}가 생성되어 있다")
    public void 이름으로_생성되어_있다(String name) throws Exception {
        // product.feature Background에서 카테고리 사전 생성용
        카테고리를_생성한다(name);
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(200);
    }

    @When("카테고리 목록을 조회한다")
    public void 카테고리_목록을_조회한다() {
        listResponse = restTemplate.getForEntity("/api/categories", String.class);
        context.setResponse(listResponse);
    }

    @And("카테고리 목록에 {string}이 포함되어 있다")
    public void 카테고리_목록에_포함되어_있다(String categoryName) throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/categories", String.class);
        JsonNode categories = objectMapper.readTree(response.getBody());

        boolean found = false;
        for (JsonNode category : categories) {
            if (categoryName.equals(category.get("name").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @And("카테고리 목록의 크기는 {int}이다")
    public void 카테고리_목록의_크기_검증(int expectedSize) throws Exception {
        JsonNode categories = objectMapper.readTree(listResponse.getBody());
        assertThat(categories.size()).isEqualTo(expectedSize);
    }
}
