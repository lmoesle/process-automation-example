package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

public final class ExcludeNonHexagonalImportOption implements ImportOption {

    /**
     * Exclude all Spring Boot AutoConfig classes and generated bpmn2code api
     *
     * @param location Location
     * @return boolean
     */
    @Override
    public boolean includes(final Location location) {
        final String uri = location.asURI().toString();

        if (uri.contains("/de/lmoesle/processautomationexample/bpmn/")) {
            return false;
        }

        return !uri.endsWith("/de/lmoesle/processautomationexample/shared/UserTaskSupportConfiguration.class")
                && !uri.endsWith("/de/lmoesle/processautomationexample/shared/tasklist/ProcessAutomationExampleApplication.class")
                && !uri.endsWith("/de/lmoesle/processautomationexample/shared/tasklist/TasklistRepository.class");
    }
}
