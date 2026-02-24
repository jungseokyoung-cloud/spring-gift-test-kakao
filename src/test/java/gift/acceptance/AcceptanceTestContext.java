package gift.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class AcceptanceTestContext {

    private final Map<String, Long> categoryIds = new HashMap<>();
    private final Map<String, Long> productIds = new HashMap<>();
    private final Map<String, Long> optionIds = new HashMap<>();
    private final Map<String, Long> memberIds = new HashMap<>();
    private ResponseEntity<?> response;

    public void putCategoryId(String name, Long id) {
        categoryIds.put(name, id);
    }

    public Long getCategoryId(String name) {
        return categoryIds.get(name);
    }

    public void putProductId(String name, Long id) {
        productIds.put(name, id);
    }

    public Long getProductId(String name) {
        return productIds.get(name);
    }

    public void putOptionId(String name, Long id) {
        optionIds.put(name, id);
    }

    public Long getOptionId(String name) {
        return optionIds.get(name);
    }

    public void putMemberId(String name, Long id) {
        memberIds.put(name, id);
    }

    public Long getMemberId(String name) {
        return memberIds.get(name);
    }

    public ResponseEntity<?> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<?> response) {
        this.response = response;
    }
}
