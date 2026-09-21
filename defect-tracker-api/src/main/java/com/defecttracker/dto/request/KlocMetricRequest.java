package com.defecttracker.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlocMetricRequest {
    @JsonAlias({"backendRepo", "backend_repo", "backend_repo_url"})
    private String backendRepoUrl;

    @JsonAlias({"frontendRepo", "frontend_repo", "frontend_repo_url"})
    private String frontendRepoUrl;

    private String githubToken;
    private String githubUsername;
    private Double calculatedKloc;
    private Long totalLinesOfCode;
}
