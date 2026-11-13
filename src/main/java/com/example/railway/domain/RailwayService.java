package com.example.railway.domain;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RailwayService {
    private final JdbcTemplate jdbc;
    public RailwayService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String,Object> getTrain(String trainId) {
        return jdbc.queryForMap("SELECT train_id,status,current_location,updated_at FROM trains WHERE train_id=?", trainId);
    }

    public List<Map<String,Object>> findTrains() {
        return jdbc.queryForList("SELECT train_id,status,current_location,updated_at FROM trains ORDER BY train_id");
    }

    @Transactional
    public Map<String,Object> createIncident(String trainId, int delayMinutes, String cause, String description) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM trains WHERE train_id=?", Integer.class, trainId);
        if (count == null || count == 0) throw new IllegalArgumentException("Unknown trainId: " + trainId);
        Long id = jdbc.queryForObject(
                "INSERT INTO incidents(train_id,delay_minutes,cause,description) VALUES (?,?,?,?) RETURNING id",
                Long.class, trainId, delayMinutes, cause, description);
        jdbc.update("UPDATE trains SET status='DELAYED',updated_at=NOW() WHERE train_id=?", trainId);
        audit(null, "api", "INCIDENT_CREATED", Map.of("incidentId", id, "trainId", trainId));
        return jdbc.queryForMap("SELECT * FROM incidents WHERE id=?", id);
    }

    public Map<String,Object> getIncident(long id) {
        return jdbc.queryForMap("SELECT * FROM incidents WHERE id=?", id);
    }

    public List<Map<String,Object>> getOpenIncidents() {
        return jdbc.queryForList("SELECT * FROM incidents WHERE status='OPEN' ORDER BY created_at DESC");
    }

    public void audit(Long workflowId, String actor, String action, Map<String,?> details) {
        jdbc.update("INSERT INTO audit_events(workflow_id,actor,action,details) VALUES (?,?,?,?::jsonb)",
                workflowId, actor, action, toJson(details));
    }

    private String toJson(Map<String,?> data) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data); }
        catch (Exception e) { return "{}"; }
    }
}
