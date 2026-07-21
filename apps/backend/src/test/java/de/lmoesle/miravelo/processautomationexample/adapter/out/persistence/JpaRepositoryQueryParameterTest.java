package de.lmoesle.miravelo.processautomationexample.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JpaRepositoryQueryParameterTest {

    @Test
    void declaresExplicitBindingsForNamedQueryParameters() {
        queryParameters(
            AktiveBenutzeraufgabeJpaRepository.class,
            BenutzerJpaRepository.class,
            ProzessEngineOutboxAuftragJpaRepository.class,
            UrlaubsantragJpaRepository.class
        ).forEach(parameter -> assertThat(parameter.getAnnotation(Param.class))
            .as("%s parameter %s", parameter.getDeclaringExecutable(), parameter.getName())
            .isNotNull());
    }

    private Stream<Parameter> queryParameters(Class<?>... repositoryTypes) {
        return Arrays.stream(repositoryTypes)
            .flatMap(repositoryType -> Arrays.stream(repositoryType.getDeclaredMethods()))
            .filter(method -> method.isAnnotationPresent(Query.class))
            .flatMap(this::parameters)
            .filter(parameter -> !Pageable.class.isAssignableFrom(parameter.getType()))
            .filter(parameter -> !Sort.class.isAssignableFrom(parameter.getType()));
    }

    private Stream<Parameter> parameters(Method method) {
        return Arrays.stream(method.getParameters());
    }
}
