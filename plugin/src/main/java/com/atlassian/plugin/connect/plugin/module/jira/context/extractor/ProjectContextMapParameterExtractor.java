package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts project parameters that can be included in webpanel's iframe url.
 */
public class ProjectContextMapParameterExtractor implements ContextMapParameterExtractor<Project>
{
    private static final String PROJECT_CONTEXT_KEY = "project";
    private ProjectSerializer projectSerializer;

    public ProjectContextMapParameterExtractor(ProjectSerializer projectSerializer)
    {
        this.projectSerializer = projectSerializer;
    }

    @Override
    public Optional<Project> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROJECT_CONTEXT_KEY))
        {
            Project project = (Project) context.get(PROJECT_CONTEXT_KEY);
            return Optional.fromNullable(project);
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<Project> serializer()
    {
        return projectSerializer;
    }
}
