package gift.acceptance;

import gift.model.CategoryRepository;
import gift.model.MemberRepository;
import gift.model.OptionRepository;
import gift.model.ProductRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonStepDefinitions {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AcceptanceTestContext context;

    @Before
    public void setUp() {
        optionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Then("응답 상태코드는 {int}이다")
    public void 응답_상태코드_검증(int statusCode) {
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(statusCode);
    }
}
