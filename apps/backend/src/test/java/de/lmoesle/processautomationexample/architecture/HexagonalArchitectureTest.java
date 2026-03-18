package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static de.lmoesle.processautomationexample.architecture.ArchUnitUtils.*;

/**
 * Architektur Entscheidung: docs/adrs/ADR_004_nutze-hexagonale-architektur.md
 */
@AnalyzeClasses(
        packages = "de.lmoesle.processautomationexample",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ExcludeNonHexagonalImportOption.class
        }
)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule all_classes_must_reside_in_hexagonal_layers =
            classes()
                    .that().resideInAnyPackage("..")
                    .should().resideInAnyPackage(
                            "de.lmoesle.processautomationexample.domain..",
                            "de.lmoesle.processautomationexample.application..",
                            "de.lmoesle.processautomationexample.adapter.."
                    )
                    .because("All classes must be organized according to hexagonal architecture layers");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_other_layers =
            noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..", "..application..")
                    .because("Domain layer should be independent and not depend on any other layer");

    @ArchTest
    static final ArchRule ports_should_be_interfaces_except_commands_and_queries =
            classes()
                    .that().resideInAnyPackage(IN_PORTS, OUT_PORTS)
                    .and().areTopLevelClasses()
                    .and().resideOutsideOfPackages("..commands..", "..queries..")
                    .should().beInterfaces()
                    .because("Ports themselves must be interfaces; command/query DTOs may be concrete classes")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule hexagonal_architecture_should_be_respected =
            Architectures.layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy(DOMAIN)
                    .layer("In-Ports").definedBy(IN_PORTS)
                    .layer("Out-Ports").definedBy(OUT_PORTS)
                    .layer("In-Adapters").definedBy(IN_ADAPTERS)
                    .layer("Out-Adapters").definedBy(OUT_ADAPTERS)
                    .layer("Application").definedBy(APPLICATION)
                    .whereLayer("In-Ports").mayOnlyBeAccessedByLayers("Application", "In-Adapters")
                    .whereLayer("Out-Ports").mayOnlyBeAccessedByLayers("Application", "Out-Adapters")
                    .whereLayer("In-Adapters").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Out-Adapters").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("In-Ports");
}
