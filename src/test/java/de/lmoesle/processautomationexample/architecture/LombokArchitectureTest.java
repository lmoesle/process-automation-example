package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

/**
 * Architektur Entscheidung: docs/adrs/ADR_011_vermeide-lombok-und-mapstruct.md
 */
@AnalyzeClasses(packages = "de.lmoesle.processautomationexample", importOptions = {ImportOption.DoNotIncludeTests.class})
public class LombokArchitectureTest {

    // ArchUnit only sees bytecode, so we have to read the source files to check for Lombok imports.
    // This is a placeholder for the actual rule that would check for Lombok usage.
}
