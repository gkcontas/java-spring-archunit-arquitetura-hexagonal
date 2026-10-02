package com.gkcontas.hexagonal.architecture;

import com.gkcontas.hexagonal.domain.exception.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * The architecture, as tests.
 *
 * <p>Every rule here could have been a paragraph in a wiki instead. The difference is that
 * a paragraph is true on the day it is written and silently false six months later, while
 * this fails the build on the commit that breaks it — which is the only moment when fixing
 * it is cheap.
 *
 * <p>{@code DoNotIncludeTests} matters: the test sources inject fields, touch standard
 * streams and reach across layers on purpose, and none of that says anything about the
 * production code.
 */
@AnalyzeClasses(
        packages = "com.gkcontas.hexagonal",
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    /**
     * The shape of the whole thing in one rule: dependencies point inward, and no adapter
     * knows another adapter exists.
     *
     * <p>The exceptions sit in the domain model layer, not in the service one: the model
     * throws them, and an inner layer may not depend on an outer one. Declaring them as
     * services compiles fine and fails this rule immediately — which is the kind of
     * mistake that a diagram would have carried for months.
     *
     * <p>{@code withOptionalLayers} is on because this model has no domain service yet:
     * every rule fits inside an aggregate. The layer is declared anyway, so the day one
     * appears it lands in a package the architecture already governs.
     */
    @ArchTest
    static final ArchRule theOnionHoldsItsShape = onionArchitecture()
            .domainModels("..domain.model..", "..domain.exception..")
            .domainServices("..domain.service..")
            .applicationServices("..application..")
            .adapter("web", "..adapter.in.web..")
            .adapter("cli", "..adapter.in.cli..")
            .adapter("persistence", "..adapter.out.persistence..")
            .adapter("configuration", "..config..")
            .withOptionalLayers(true);

    /**
     * The rule the whole project exists to demonstrate.
     *
     * <p>A core that "has no framework dependency" is a claim anyone can break with a
     * single convenient import — an {@code @Component} to get injection, a
     * {@code @Column} to skip writing a mapper. Each one is reasonable on its own and
     * together they end the separation.
     */
    @ArchTest
    static final ArchRule theDomainKnowsNoFramework = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "javax..",
                    "com.fasterxml..",
                    "org.hibernate..",
                    "com.gkcontas.hexagonal.application..",
                    "com.gkcontas.hexagonal.adapter..",
                    "com.gkcontas.hexagonal.config..");

    /**
     * The application orchestrates the domain and talks to its own ports — never to the
     * things that implement them.
     */
    @ArchTest
    static final ArchRule theApplicationKnowsNoAdapter = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.gkcontas.hexagonal.adapter..",
                    "com.gkcontas.hexagonal.config..",
                    "org.springframework..",
                    "jakarta..");

    /** Dependency inversion, stated: whoever implements an outbound port is an adapter. */
    @ArchTest
    static final ArchRule outboundPortsAreImplementedByAdapters = classes()
            .that().implement(resideInAPackage("..application.port.out.."))
            .should().resideInAPackage("..adapter.out..");

    /**
     * An inbound adapter goes through a use case, never straight to a repository.
     *
     * <p>Skipping the use case is the shortcut that turns a controller into the place
     * where business rules quietly accumulate.
     */
    @ArchTest
    static final ArchRule inboundAdaptersGoThroughUseCases = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..application.port.out..");

    @ArchTest
    static final ArchRule packagesAreFreeOfCycles = slices()
            .matching("com.gkcontas.hexagonal.(**)")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule inboundPortsAreNamedUseCase = classes()
            .that().resideInAPackage("..application.port.in..").and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase");

    @ArchTest
    static final ArchRule applicationServicesImplementAnInboundPort = classes()
            .that().resideInAPackage("..application.service..")
            .should().haveSimpleNameEndingWith("Service")
            .andShould().implement(resideInAPackage("..application.port.in.."));

    @ArchTest
    static final ArchRule businessFailuresShareOneHierarchy = classes()
            .that().resideInAPackage("..domain.exception..")
            .should().beAssignableTo(DomainException.class);

    /** Persistence annotations stay in the persistence adapter. */
    @ArchTest
    static final ArchRule entitiesLiveOnlyInThePersistenceAdapter = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..adapter.out.persistence.jpa..");

    /**
     * Field injection hides a dependency from the constructor, which is also where a test
     * would have supplied it. Constructor injection makes an over-complicated class
     * uncomfortable to build, which is useful feedback.
     */
    @ArchTest
    static final ArchRule noFieldInjection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule noGenericExceptions =
            GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static final ArchRule noJavaUtilLogging =
            GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    /**
     * Printing to the console is the command line adapter's job and nobody else's.
     *
     * <p>The exemption is the interesting part: a blanket ban would be wrong here, so the
     * rule names who is allowed instead of being switched off.
     */
    @ArchTest
    static final ArchRule onlyTheCliWritesToTheConsole = noClasses()
            .that().resideOutsideOfPackage("..adapter.in.cli..")
            .should(GeneralCodingRules.ACCESS_STANDARD_STREAMS);

    @ArchTest
    static final ArchRule noLegacyDateApi = noClasses()
            .should().dependOnClassesThat(JavaClass.Predicates.type(java.util.Date.class)
                    .or(JavaClass.Predicates.type(java.util.Calendar.class)));
}
