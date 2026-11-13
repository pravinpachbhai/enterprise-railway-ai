package com.example.railway.agent;

import com.example.railway.rag.PolicyRagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RailwayAgentService {
    private final ChatClient chatClient;
    private final PolicyRagService rag;

    public RailwayAgentService(ChatClient.Builder builder, PolicyRagService rag) {
        this.chatClient = builder.build();
        this.rag = rag;
    }

    public Map<String,Object> chat(String message) {
        List<Map<String,Object>> docs = rag.search(message, 4);
        String context = docs.isEmpty() ? "No matching policy documents were found." :
                docs.stream().map(d -> String.valueOf(d.get("content"))).reduce((a,b) -> a + "\\n---\\n" + b).orElse("");
        String answer = chatClient.prompt()
                .system("""
                    You are a railway operations assistant. Use only the provided policy context for policy claims.
                    Clearly say when context is insufficient. Do not claim a notification was sent.
                    Do not invent train status, affected services, recipients, or policy rules.
                    Operational actions require the separate workflow API and approval.
                    """)
                .user("Question: " + message + "\\n\\nRetrieved policy context:\\n" + context)
                .call().content();
        return Map.of("answer", answer == null ? "" : answer, "retrievedDocuments", docs.size(), "policyContext", docs);
    }
}
