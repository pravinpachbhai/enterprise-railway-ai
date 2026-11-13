package com.example.railway.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ApiModels {
    private ApiModels() {}

    public record CreateIncidentRequest(
            @NotBlank String trainId,
            @Min(0) int delayMinutes,
            @NotBlank String cause,
            @NotBlank String description) {}

    public record CreateWorkflowRequest(@NotNull Long incidentId) {}
    public record ApprovalRequest(@NotBlank String approver) {}
    public record ChatRequest(@NotBlank String message) {}
    public record IngestRequest(@NotBlank String title, @NotBlank String content) {}
    public record SearchRequest(@NotBlank String query, Integer topK) {}
}
