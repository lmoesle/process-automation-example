package de.lmoesle.processautomationexample.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

public final class ExcludeNonModalImportOption implements ImportOption {

    /**
     * Exclude all Spring Boot AutoConfig classes and generated bpmn2code api
     *
     * @param location Location
     * @return boolean
     */
    @Override
    public boolean includes(final Location location) {
        final String uri = location.asURI().toString();

        return !uri.endsWith("/de/lmoesle/processautomationexample/adapter/in/rest/dto/UrlaubsantragStatusDto.class");
    }
}
