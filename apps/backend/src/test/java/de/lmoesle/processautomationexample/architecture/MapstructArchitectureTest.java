package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architektur Entscheidung: docs/adrs/ADR_011_vermeide-lombok-und-mapstruct.md
 */
@AnalyzeClasses(packages = "de.lmoesle.processautomationexample", importOptions = {ImportOption.DoNotIncludeTests.class})
public class MapstructArchitectureTest {

    @ArchTest
    static final ArchRule no_dependency_on_mapstruct = noClasses()
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.mapstruct..", "org.mapstruct.factory..", "org.mapstruct.ap..");
}
