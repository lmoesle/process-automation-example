package de.lmoesle.processautomationexample.domain.tasklist;

import de.lmoesle.processautomationexample.bpmn.VacationApprovalProcessApi;
import de.lmoesle.processautomationexample.domain.benutzer.BenutzerTestdaten;
import de.lmoesle.processautomationexample.domain.urlaubsantrag.UrlaubsantragTestData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UserTaskTestdaten {

    public static final String TASK_ID = "approve-vacation-1";
    public static final String SECOND_TASK_ID = "approve-vacation-2";

    private UserTaskTestdaten() {
    }

    public static UserTaskId taskId() {
        return UserTaskId.of(TASK_ID);
    }

    public static UserTaskId secondTaskId() {
        return UserTaskId.of(SECOND_TASK_ID);
    }

    public static Map<String, String> meta() {
        LinkedHashMap<String, String> meta = new LinkedHashMap<>();
        meta.put("processDefinitionKey", VacationApprovalProcessApi.PROCESS_ID);
        meta.put("assignee", BenutzerTestdaten.ADA_UUID.toString());
        meta.put("formKey", "embedded:app:forms/vacation-approval.html");
        return meta;
    }

    public static Map<String, String> secondMeta() {
        LinkedHashMap<String, String> meta = new LinkedHashMap<>();
        meta.put("processDefinitionKey", VacationApprovalProcessApi.PROCESS_ID);
        meta.put("assignee", BenutzerTestdaten.CARLA_UUID.toString());
        meta.put("formKey", "embedded:app:forms/vacation-approval.html");
        return meta;
    }

    public static Map<String, Object> payload() {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put(VacationApprovalProcessApi.Variables.URLAUBSANTRAG_ID, UrlaubsantragTestData.VACATION_REQUEST_UUID.toString());
        payload.put("teamLeadIds", List.of(BenutzerTestdaten.ADA_UUID.toString(), BenutzerTestdaten.CARLA_UUID.toString()));
        payload.put("requester", "Ada Lovelace");
        payload.put("days", 5);
        return payload;
    }

    public static Map<String, Object> secondPayload() {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put(VacationApprovalProcessApi.Variables.URLAUBSANTRAG_ID, UrlaubsantragTestData.SECOND_VACATION_REQUEST_UUID.toString());
        payload.put("teamLeadIds", List.of(BenutzerTestdaten.CARLA_UUID.toString()));
        payload.put("requester", "Grace Hopper");
        payload.put("days", 10);
        return payload;
    }

    public static UserTask userTask() {
        return new UserTask(
            taskId(),
            UrlaubsantragTestData.urlaubsantragWithStartedProcess(),
            List.of(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
            BenutzerTestdaten.ada()
        );
    }

    public static UserTask secondUserTask() {
        return new UserTask(
            secondTaskId(),
            UrlaubsantragTestData.secondUrlaubsantrag(BenutzerTestdaten.ada(), BenutzerTestdaten.carla()),
            List.of(BenutzerTestdaten.carla()),
            BenutzerTestdaten.carla()
        );
    }

    public static UserTask userTaskWithoutPayload() {
        return userTask();
    }

    public static UserTask secondUserTaskWithoutPayload() {
        return secondUserTask();
    }
}
