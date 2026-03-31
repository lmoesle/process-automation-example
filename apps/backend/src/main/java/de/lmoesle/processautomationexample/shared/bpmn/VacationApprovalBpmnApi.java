package de.lmoesle.processautomationexample.shared.bpmn;

public final class VacationApprovalBpmnApi {

    public static final String MESSAGE_AUTOMATIC_CHECK_RESULT_ID = "Message_automatic_check_result";
    public static final String MESSAGE_AUTOMATIC_CHECK_RESULT_NAME = "automatic_check_result";

    public static final String PROCESS_ID = "vacation_approval";
    public static final String PROCESS_NAME = "Urlaubsantrag";
    public static final int PROCESS_HISTORY_TIME_TO_LIVE_DAYS = 180;

    public static final String PROCESS_VARIABLE_VACATION_REQUEST_ID = "urlaubsantragId";
    public static final String PROCESS_VARIABLE_TEAM_LEAD_IDS = "teamLeadIds";
    public static final String PROCESS_VARIABLE_VALID = "gueltig";
    public static final String PROCESS_VARIABLE_APPROVED = "genehmigt";

    public static final String START_EVENT_ID = "StartEvent_1";
    public static final String START_EVENT_NAME = "Urlaubsantrag stellen";
    public static final String FLOW_START_TO_AUTOMATIC_CHECK_ID = "Flow_11hl1ga";

    public static final String AUTOMATIC_CHECK_TASK_ID = "automatic_check";
    public static final String AUTOMATIC_CHECK_TASK_NAME = "Automatisch Prüfung des Antrags";
    public static final String AUTOMATIC_CHECK_TASK_TYPE = "external";
    public static final String AUTOMATIC_CHECK_TASK_TOPIC = "automatic_check";
    public static final String FLOW_AUTOMATIC_CHECK_TO_VALID_GATEWAY_ID = "Flow_0sirzwj";

    public static final String VALID_GATEWAY_ID = "Gateway_01y0omz";
    public static final String VALID_GATEWAY_NAME = "Ist der Antrag gülting?";
    public static final String FLOW_VALID_YES_ID = "Flow_1sa7ezg";
    public static final String FLOW_VALID_YES_NAME = "Ja";
    public static final String FLOW_VALID_YES_CONDITION = "${gueltig == true}";
    public static final String FLOW_VALID_NO_ID = "Flow_18do1vp";
    public static final String FLOW_VALID_NO_NAME = "Nein";
    public static final String FLOW_VALID_NO_CONDITION = "${gueltig == false}";

    public static final String REJECTED_AFTER_AUTOMATIC_CHECK_END_EVENT_ID = "Event_1a2z938";
    public static final String REJECTED_AFTER_AUTOMATIC_CHECK_END_EVENT_NAME = "Abgelehnt";

    public static final String SUPERVISOR_APPROVAL_TASK_ID = "Activity_0tid0x1";
    public static final String SUPERVISOR_APPROVAL_TASK_NAME = "Genehmigung von Vorgesetztem";
    public static final String SUPERVISOR_APPROVAL_TASK_CANDIDATE_USERS_EXPRESSION = "${teamLeadIds}";
    public static final String FLOW_SUPERVISOR_APPROVAL_TO_APPROVED_GATEWAY_ID = "Flow_1st3ry8";

    public static final String APPROVED_GATEWAY_ID = "Gateway_18nqz6r";
    public static final String APPROVED_GATEWAY_NAME = "Genehmigt";
    public static final String FLOW_APPROVED_YES_ID = "Flow_0blczzw";
    public static final String FLOW_APPROVED_YES_NAME = "Ja";
    public static final String FLOW_APPROVED_YES_CONDITION = "${genehmigt == true}";
    public static final String FLOW_APPROVED_NO_ID = "Flow_1cyfud8";
    public static final String FLOW_APPROVED_NO_NAME = "Nein";
    public static final String FLOW_APPROVED_NO_CONDITION = "${genehmigt == false}";

    public static final String APPROVED_END_EVENT_ID = "Event_1x1pzdi";
    public static final String APPROVED_END_EVENT_NAME = "Genehmigt";
    public static final String REJECTED_AFTER_SUPERVISOR_APPROVAL_END_EVENT_ID = "Event_0t9knc8";
    public static final String REJECTED_AFTER_SUPERVISOR_APPROVAL_END_EVENT_NAME = "Abgelehnt";

    public static final String AUTOMATIC_CHECK_RESULT_EVENT_SUB_PROCESS_ID = "EventSubProcess_automatic_check_result";
    public static final String AUTOMATIC_CHECK_RESULT_START_EVENT_ID = "Event_automatic_check_result";
    public static final String AUTOMATIC_CHECK_RESULT_MESSAGE_EVENT_DEFINITION_ID = "MessageEventDefinition_automatic_check_result";
    public static final String FLOW_AUTOMATIC_CHECK_RESULT_EVENT_ID = "Flow_automatic_check_result_event";
    public static final String AUTOMATIC_CHECK_RESULT_END_EVENT_ID = "Event_automatic_check_result_end";

    private VacationApprovalBpmnApi() {
    }
}
