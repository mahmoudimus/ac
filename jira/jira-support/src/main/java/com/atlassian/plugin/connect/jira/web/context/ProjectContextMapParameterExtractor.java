package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.web.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.spi.web.context.ParameterSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

/**
 * Extracts project parameters that can be included in webpanel's iframe url.
 */
@JiraComponent
public class ProjectContextMapParameterExtractor implements ContextMapParameterExtractor<Project> {
    private static final String PROJECT_CONTEXT_KEY = "project";
    private ProjectSerializer projectSerializer;

    @Autowired
    public ProjectContextMapParameterExtractor(ProjectSerializer projectSerializer) {
        this.projectSerializer = projectSerializer;
    }

    @Override
    public Optional<Project> extract(final Map<String, Object> context) {
        if (context.containsKey(PROJECT_CONTEXT_KEY)) {
            Project project = (Project) context.get(PROJECT_CONTEXT_KEY);
            return Optional.ofNullable(project);
        }
        return Optional.empty();
    }

    @Override
    public ParameterSerializer<Project> serializer() {
        return projectSerializer;
    }
}
