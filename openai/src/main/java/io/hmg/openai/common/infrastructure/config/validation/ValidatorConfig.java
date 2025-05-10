package io.hmg.openai.common.infrastructure.config.validation;

import jakarta.validation.Validation;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class ValidatorConfig implements WebFluxConfigurer {

    @Override
    public Validator getValidator() {
        return new SpringValidatorAdapter(
                Validation.buildDefaultValidatorFactory().getValidator()
        );
    }
}