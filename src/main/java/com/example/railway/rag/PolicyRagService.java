package com.example.railway.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PolicyRagService {
    private final VectorStore vectorStore;
    public PolicyRagService(VectorStore vectorStore) { this.vectorStore = vectorStore; }

    public Map<String,Object> ingest(String title, String content) {
        Document doc = new Document(content, Map.of("title", title, "documentType", "railway-policy"));
        vectorStore.add(List.of(doc));
        return Map.of("status", "INGESTED", "title", title, "characters", content.length());
    }

    public List<Map<String,Object>> search(String query, int topK) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(Math.max(1, Math.min(topK, 10))).build());
        return docs.stream().map(d -> {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("content", d.getText());
            row.put("metadata", d.getMetadata());
            row.put("id", d.getId());
            return row;
        }).toList();
    }
}
