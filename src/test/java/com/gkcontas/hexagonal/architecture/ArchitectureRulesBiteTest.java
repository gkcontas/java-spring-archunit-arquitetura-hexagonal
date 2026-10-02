package com.gkcontas.hexagonal.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests that the architecture tests work.
 *
 * <p>A suite of rules that passes tells you one of two things: the code respects them, or
 * the rules match nothing. The second is easy to arrive at by accident — one typo in a
 * package pattern and a rule guards an empty set forever, green and useless.
 *
 * <p>So the rules are also pointed at code that deliberately breaks them, under
 * {@code architecture.sample}, and asserted to fail. Those classes live in test sources
 * and are kept out of the real analysis by {@code ImportOption.DoNotIncludeTests}.
 */
class ArchitectureRulesBiteTest {

    private static final JavaClasses SAMPLE = new ClassFileImporter()
            .importPackages("com.gkcontas.archsample");

    @Test
    void shouldRejectAFrameworkAnnotationInTheDomain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..archsample.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

        assertThatThrownBy(() -> rule.check(SAMPLE))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("LeakyRoom");
    }

    @Test
    void shouldRejectAnOutboundPortImplementedOutsideAnAdapter() {
        ArchRule rule = classes()
                .that().implement(resideInAPackage("..application.port.out.."))
                .and().resideInAPackage("..archsample..")
                .should().resideInAPackage("..adapter.out..");

        assertThatThrownBy(() -> rule.check(SAMPLE))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("MisplacedPortImplementation");
    }

    @Test
    void shouldPassWhenTheCodeRespectsTheRule() {
        // The control case: the same rule, pointed at code that does not break it, must
        // not fail — otherwise the two assertions above would prove nothing either.
        ArchRule rule = noClasses()
                .that().resideInAPackage("..archsample.application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..");

        rule.check(SAMPLE);
    }
}
