package com.spottrack.platform.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * RF-3: a bounded context talks to another one only through its ACL facade
 * ({@code <bc>.interfaces.acl}) or its integration events ({@code <bc>.interfaces.events}),
 * never through its domain model. {@code shared} is the shared kernel and may be used by anyone.
 *
 * <p>Enforced for the contexts already migrated. analytics and notification still read the gym
 * domain through deprecated facade methods; they are tracked as RF-6 and get added here once fixed.
 */
class BoundedContextBoundariesTest {

    private static final String ROOT = "com.spottrack.platform";

    private static final List<String> BOUNDED_CONTEXTS = List.of(
            "analytics", "gym", "iam", "maintenance", "membership", "monitoring",
            "notification", "profiles", "reservation", "routine");

    private static JavaClasses productionClasses;

    @BeforeAll
    static void importClasses() {
        productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @ParameterizedTest(name = "{0} does not depend on another context's domain")
    @ValueSource(strings = {"reservation", "maintenance", "monitoring", "membership"})
    void doesNotDependOnTheDomainOfAnotherBoundedContext(String context) {
        var foreignDomains = BOUNDED_CONTEXTS.stream()
                .filter(other -> !other.equals(context))
                .map(other -> ROOT + "." + other + ".domain..")
                .toArray(String[]::new);

        noClasses()
                .that().resideInAPackage(ROOT + "." + context + "..")
                .should().dependOnClassesThat().resideInAnyPackage(foreignDomains)
                .because("bounded contexts must talk through ACL facades and integration events (RF-3)")
                .check(productionClasses);
    }
}
