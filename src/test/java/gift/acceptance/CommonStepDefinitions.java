package gift.acceptance;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonStepDefinitions {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AcceptanceTestContext context;

    @Before
    public void setUp() {
        databaseCleaner.clear();
    }

    @Then("응답 상태코드는 {int}이다")
    public void 응답_상태코드_검증(int statusCode) {
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(statusCode);
    }
}
