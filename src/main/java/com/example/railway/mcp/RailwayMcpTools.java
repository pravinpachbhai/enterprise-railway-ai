package com.example.railway.mcp;

import com.example.railway.domain.RailwayService;
import com.example.railway.rag.PolicyRagService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RailwayMcpTools {
    private final RailwayService railway;
    private final PolicyRagService rag;

    public RailwayMcpTools(RailwayService railway, PolicyRagService rag) {
        this.railway = railway; this.rag = rag;
    }

    @McpTool(description = "Get the stored operational status and location of a railway train. Read-only.")
    public Map<String,Object> getTrainStatus(
            @McpToolParam(description = "Train identifier, for example BNSF-7821") String trainId) {
        return railway.getTrain(trainId);
    }

    @McpTool(description = "List open railway incidents from the operational database. Read-only.")
    public List<Map<String,Object>> listOpenIncidents() {
        return railway.getOpenIncidents();
    }

    @McpTool(description = "Search ingested railway notification policy documents. Read-only.")
    public List<Map<String,Object>> searchNotificationPolicy(
            @McpToolParam(description = "Natural-language query about a railway policy") String query) {
        return rag.search(query, 4);
    }
}
