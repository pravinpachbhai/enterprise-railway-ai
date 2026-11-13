package com.example.railway.workflow;

import com.example.railway.domain.RailwayService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class NotificationWorkflowService {
    private final JdbcTemplate jdbc;
    private final RailwayService railway;

    public NotificationWorkflowService(JdbcTemplate jdbc, RailwayService railway) {
        this.jdbc = jdbc; this.railway = railway;
    }

    @Transactional
    public Map<String,Object> create(long incidentId) {
        Map<String,Object> incident = railway.getIncident(incidentId);
        String trainId = String.valueOf(incident.get("train_id"));
        int delay = ((Number) incident.get("delay_minutes")).intValue();
        String cause = String.valueOf(incident.get("cause"));
        String description = String.valueOf(incident.get("description"));
        List<Map<String,Object>> plan = new ArrayList<>();
        plan.add(Map.of("recipient","operations-desk@example.invalid",
                "message","Railway incident: train " + trainId + " delayed " + delay +
                        " minutes. Cause: " + cause + ". Details: " + description));
        if (delay >= 30) {
            plan.add(Map.of("recipient","customer-operations@example.invalid",
                    "message","Service advisory: train " + trainId + " has a verified reported delay of " +
                            delay + " minutes. Please consult operations for current impact."));
        }
        Long workflowId = jdbc.queryForObject(
                "INSERT INTO notification_workflows(incident_id,state,plan_json) VALUES (?,'AWAITING_APPROVAL',?::jsonb) RETURNING id",
                Long.class, incidentId, json(plan));
        railway.audit(workflowId, "system", "WORKFLOW_CREATED",
                Map.of("incidentId", incidentId, "plannedNotifications", plan.size()));
        return get(workflowId);
    }

    @Transactional
    public Map<String,Object> approve(long workflowId, String approver) {
        Map<String,Object> workflow = get(workflowId);
        if (!"AWAITING_APPROVAL".equals(workflow.get("state"))) {
            throw new IllegalStateException("Workflow is not awaiting approval");
        }
        List<Map<String,Object>> plan = readPlan(workflow.get("plan_json"));
        jdbc.update("UPDATE notification_workflows SET state='DISPATCHING',updated_at=NOW() WHERE id=?", workflowId);
        for (int i = 0; i < plan.size(); i++) {
            Map<String,Object> item = plan.get(i);
            String recipient = String.valueOf(item.get("recipient"));
            String message = String.valueOf(item.get("message"));
            String key = "workflow-" + workflowId + "-notification-" + i;
            jdbc.update("""
                INSERT INTO notifications(workflow_id,recipient,message,idempotency_key,status)
                VALUES (?,?,?,?, 'SENT')
                ON CONFLICT (idempotency_key) DO NOTHING
                """, workflowId, recipient, message, key);
        }
        jdbc.update("UPDATE notification_workflows SET state='SENT',updated_at=NOW() WHERE id=?", workflowId);
        railway.audit(workflowId, approver, "WORKFLOW_APPROVED_AND_MOCK_DISPATCHED",
                Map.of("notificationCount", plan.size(), "mode", "MOCK"));
        return get(workflowId);
    }

    public Map<String,Object> get(long workflowId) {
        return jdbc.queryForMap("SELECT * FROM notification_workflows WHERE id=?", workflowId);
    }

    public List<Map<String,Object>> notifications(long workflowId) {
        return jdbc.queryForList("SELECT id,recipient,message,status,idempotency_key,created_at FROM notifications WHERE workflow_id=? ORDER BY id", workflowId);
    }

    public List<Map<String,Object>> audit(long workflowId) {
        return jdbc.queryForList("SELECT actor,action,details,created_at FROM audit_events WHERE workflow_id=? ORDER BY id", workflowId);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> readPlan(Object raw) {
        try {
            String value = raw instanceof org.postgresql.util.PGobject p ? p.getValue() : String.valueOf(raw);
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(value,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>() {});
        } catch (Exception e) { throw new IllegalStateException("Cannot read workflow plan", e); }
    }

    private String json(Object value) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value); }
        catch (Exception e) { throw new IllegalArgumentException("Cannot serialize plan", e); }
    }
}
