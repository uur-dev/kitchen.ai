package validation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ExistsInDatabaseValidator
        implements ConstraintValidator<ExistsInDatabase, Object> {

    @PersistenceContext
    private EntityManager entityManager;

    private Class<?> entity;
    private String field;

    @Override
    public void initialize(ExistsInDatabase annotation) {
        this.entity = annotation.entity();
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull separately
        if (value == null) return true;

        try {
            String jpql = "SELECT COUNT(e) FROM "
                    + entity.getSimpleName()
                    + " e WHERE e." + field + " = :value";

            TypedQuery<Long> query = entityManager
                    .createQuery(jpql, Long.class)
                    .setParameter("value", value);

            return query.getSingleResult() > 0;

        } catch (Exception ex) {
            // Log the exception in a real project
            return false;
        }
    }
}