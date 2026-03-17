package de.lmoesle.processautomationexample.adapter.in.process;

import de.lmoesle.processautomationexample.domain.vacationrequest.ProcessInstanceId;
import dev.bpmcrafters.processengine.worker.registrar.VariableConverter;

public class ProcessInstanceIdWorkerVariableConverter implements VariableConverter {

    @Override
    public <T> T mapToType(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (!ProcessInstanceId.class.equals(type)) {
            throw new IllegalArgumentException("Unsupported variable target type: " + type.getName());
        }

        if (value instanceof ProcessInstanceId processInstanceId) {
            return type.cast(processInstanceId);
        }

        if (value instanceof String stringValue) {
            return type.cast(ProcessInstanceId.of(stringValue));
        }

        throw new IllegalArgumentException("Unsupported processInstanceId variable value: " + value.getClass().getName());
    }
}
