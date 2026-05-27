package com.teamgannon.trips.config.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the auto-configured Jackson 3 {@code JsonMapper} so that all
 * fields are visible to (de)serialisation regardless of getter/setter
 * presence.
 * <p>
 * Previously this class registered a competing {@code @Primary
 * @Bean ObjectMapper} alongside Spring Boot 4's auto-configured
 * {@code jacksonJsonMapper} — both marked {@code @Primary}, which made
 * Spring fail to resolve {@code ObjectMapper} injections with
 * {@code NoUniqueBeanDefinitionException: more than one 'primary' bean
 * found among candidates: [objectMapper, jacksonJsonMapper]}.
 * <p>
 * The Spring Boot 4 / Jackson 3 idiom is to contribute a
 * {@link JsonMapperBuilderCustomizer} that the auto-config applies to its
 * own mapper builder — that way there's still a single primary bean
 * (the auto-configured one) and our visibility tweak is baked into it.
 */
@Configuration
public class JSONConfiguration {

    /**
     * Make Jackson see all properties — including private fields without
     * matching getters/setters. Needed for the project's entity classes,
     * which mix Lombok-generated accessors with internal-only fields.
     */
    @Bean
    public JsonMapperBuilderCustomizer tripsAnyVisibilityCustomizer() {
        return builder -> builder.changeDefaultVisibility(visibility ->
                visibility.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY));
    }
}
