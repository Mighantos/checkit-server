package com.github.checkit.service;

import com.github.checkit.config.properties.GitHubConfigProperties;
import com.github.checkit.config.properties.SgovConfigProperties;
import com.github.checkit.exception.SgovPublishException;
import com.github.checkit.model.ProjectContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class SGoVServerService {

    private final boolean allowedToPublishToSSP;
    private final String sgovServerUrl;
    private final String doNotPublishUrl = "http://DoNotPublishToSSP/";
    private final String publishPathTemplate = "workspaces/%s/publish";
    private final String pullRequestHeaderName = "Location";

    /**
     * Constructor.
     */
    public SGoVServerService(SgovConfigProperties sgovConfigProperties,
                             GitHubConfigProperties gitHubConfigProperties) {
        this.allowedToPublishToSSP = gitHubConfigProperties.getPublishToSSP();
        this.sgovServerUrl = makeSureSgovUrlEndsWithSlash(sgovConfigProperties.getUrl());
    }

    /**
     * Calls endpoint of SGoV server that creates a Pull Request to SSP in GitHub.
     *
     * @param projectContext Project context
     * @return Pull Request identifier
     */
    public String createPullRequest(ProjectContext projectContext) {
        if (!allowedToPublishToSSP) {
            return doNotPublishUrl;
        }
        String prUrl = null;
        String exceptionMessage = "Returned Pull Request URL is null.";
        try {
            String publishPath = String.format(publishPathTemplate, projectContext.getId());
            String token =
                ((Jwt) SecurityContextHolder.getContext().getAuthentication().getCredentials()).getTokenValue();

            URL sgovUrl = new URL(sgovServerUrl + publishPath);
            HttpURLConnection sgovConnection = (HttpURLConnection) sgovUrl.openConnection();
            sgovConnection.setRequestMethod("POST");
            sgovConnection.setRequestProperty("accept", "application/json");
            sgovConnection.setRequestProperty("authorization", "Bearer " + token);
            int responseCode = sgovConnection.getResponseCode();
            Map<String, List<String>> headerFields = sgovConnection.getHeaderFields();
            if (responseCode == 201 && headerFields.containsKey(pullRequestHeaderName)) {
                prUrl = headerFields.get(pullRequestHeaderName).iterator().next();
            } else if (responseCode < 300) {
                BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(sgovConnection.getInputStream()));
                StringBuffer responseBody = new StringBuffer();
                bufferedReader.lines().forEach(responseBody::append);
                exceptionMessage =
                    String.format("SGoV server did not provide Pull Request URL.\n SGoV responded %s: %s",
                        responseCode, responseBody);
            } else {
                exceptionMessage =
                    String.format("Publish on SGoV server failed with status code %s.\n %s", responseCode,
                        new BufferedReader(new InputStreamReader(sgovConnection.getErrorStream())).readLine());
            }
            sgovConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SgovPublishException("Communication with SGoV server failed.");
        }
        if (Objects.isNull(prUrl)) {
            throw new SgovPublishException(exceptionMessage);
        }
        return prUrl;
    }

    private String makeSureSgovUrlEndsWithSlash(String sgovServerUrl) {
        if (sgovServerUrl.endsWith("/")) {
            return sgovServerUrl;
        }
        return sgovServerUrl + "/";
    }
}
