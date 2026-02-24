package gift.acceptance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleaner {

    private static final List<String> TABLES = List.of(
            "wish", "options", "product", "member", "category"
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void clear() {
        entityManager.flush();
        for (String table : TABLES) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + table + " CASCADE").executeUpdate();
        }
    }
}
