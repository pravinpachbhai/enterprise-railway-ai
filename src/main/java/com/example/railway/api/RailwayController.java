package com.example.railway.api;

import com.example.railway.agent.RailwayAgentService;
import com.example.railway.domain.RailwayService;
import com.example.railway.rag.PolicyRagService;
import com.example.railway.workflow.NotificationWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import static com.example.railway.api.ApiModels.*;

@RestController
@RequestMapping("/api")
public class RailwayController {
    private final RailwayService railway;
    private final NotificationWorkflowService workflows;
    private final PolicyRagService rag;
    private final RailwayAgentService agent;

    public RailwayController(RailwayService railway, NotificationWorkflowService workflows,
                             PolicyRagService rag, RailwayAgentService agent) {
        this.railway = railway; this.workflows = workflows; this.rag = rag; this.agent = agent;
    }

    @GetMapping("/trains")
    public List<Map<String,Object>> trains() { return railway.findTrains(); }

    @GetMapping("/trains/{trainId}")
    public Map<String,Object> train(@PathVariable String trainId) { return railway.getTrain(trainId); }

    @PostMapping("/incidents")
    public ResponseEntity<Map<String,Object>> createIncident(@Valid @RequestBody CreateIncidentRequest r) {
        return ResponseEntity.ok(railway.createIncident(r.trainId(), r.delayMinutes(), r.cause(), r.description()));
    }

    @GetMapping("/incidents/{id}")
    public Map<String,Object> incident(@PathVariable long id) { return railway.getIncident(id); }

    @PostMapping("/notifications/workflows")
    public ResponseEntity<Map<String,Object>> createWorkflow(@Valid @RequestBody CreateWorkflowRequest r) {
        return ResponseEntity.ok(workflows.create(r.incidentId()));
    }

    @GetMapping("/notifications/workflows/{id}")
    public Map<String,Object> workflow(@PathVariable long id) { return workflows.get(id); }

    @PostMapping("/notifications/workflows/{id}/approve")
    public Map<String,Object> approve(@PathVariable long id, @Valid @RequestBody ApprovalRequest r) {
        return workflows.approve(id, r.approver());
    }

    @GetMapping("/notifications/workflows/{id}/notifications")
    public List<Map<String,Object>> notifications(@PathVariable long id) { return workflows.notifications(id); }

    @GetMapping("/notifications/workflows/{id}/audit")
    public List<Map<String,Object>> audit(@PathVariable long id) { return workflows.audit(id); }

    @PostMapping("/rag/ingest")
    public Map<String,Object> ingest(@Valid @RequestBody IngestRequest r) {
        return rag.ingest(r.title(), r.content());
    }

    @PostMapping("/rag/search")
    public Map<String,Object> search(@Valid @RequestBody SearchRequest r) {
        int k = r.topK() == null ? 4 : r.topK();
        return Map.of("results", rag.search(r.query(), k));
    }

    @PostMapping("/agent/chat")
    public Map<String,Object> chat(@Valid @RequestBody ChatRequest r) {
        return agent.chat(r.message());
    }
}
