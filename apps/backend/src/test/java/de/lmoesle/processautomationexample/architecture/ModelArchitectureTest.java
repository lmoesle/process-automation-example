package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.lmoesle.processautomationexample.architecture.ArchUnitUtils.*;

@AnalyzeClasses(packages = "de.lmoesle.processautomationexample", importOptions = {ImportOption.DoNotIncludeTests.class, ExcludeNonModalImportOption.class})
public class ModelArchitectureTest {

    @ArchTest
    static final ArchRule entities_must_be_classes = classes()
            .that().resideInAnyPackage(OUT_ADAPTERS + "entities..")
            .should(BE_CLASSES);

    @ArchTest
    static final ArchRule adapter_model_must_be_records = classes()
            .that().resideInAnyPackage(
                    IN_ADAPTERS + "model..",
                    IN_ADAPTERS + "dto..",
                    OUT_ADAPTERS + "model.."
            )
            .should(BE_RECORDS_OR_INTERFACES);
}
