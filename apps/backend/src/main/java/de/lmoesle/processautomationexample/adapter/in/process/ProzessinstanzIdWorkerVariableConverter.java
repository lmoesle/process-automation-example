package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.domain.urlaubsantrag.ProzessinstanzId;
import dev.bpmcrafters.processengine.worker.registrar.VariableConverter;

public class ProzessinstanzIdWorkerVariableConverter implements VariableConverter {

    @Override
    public <T> T mapToType(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (!ProzessinstanzId.class.equals(type)) {
            throw new IllegalArgumentException("Unsupported variable target type: " + type.getName());
        }

        if (value instanceof ProzessinstanzId prozessinstanzId) {
            return type.cast(prozessinstanzId);
        }

        if (value instanceof String stringValue) {
            return type.cast(ProzessinstanzId.of(stringValue));
        }

        throw new IllegalArgumentException("Unsupported prozessinstanzId variable value: " + value.getClass().getName());
    }
}
