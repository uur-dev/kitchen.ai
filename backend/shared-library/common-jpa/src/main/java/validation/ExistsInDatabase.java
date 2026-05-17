package validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistsInDatabaseValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsInDatabase {

    String message() default "Value does not exist in the database";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Which entity/table to check
    Class<?> entity();

    // Which field/column to look up
    String field() default "id";
}