package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.bc.project.ProjectService.GetProjectResult;

/**
 * Serializes Project objects.
 */
public class ProjectSerializer extends AbstractJiraParameterSerializer<Project, GetProjectResult>
{

    public static final String PROJECT_FIELD_NAME = "project";

    public ProjectSerializer(final ProjectService projectService, UserManager userManager)
    {
        super(userManager, PROJECT_FIELD_NAME,
                new ParameterUnwrapper<GetProjectResult, Project>()
                {
                    @Override
                    public Project unwrap(GetProjectResult wrapped)
                    {
                        return wrapped.getProject();
                    }
                },
                new AbstractJiraIdParameterLookup<GetProjectResult>()
                {
                    @Override
                    public GetProjectResult lookup(User user, Long id)
                    {
                        return projectService.getProjectById(user, id);
                    }
                },
                new AbstractJiraKeyParameterLookup<GetProjectResult>()
                {
                    @Override
                    public GetProjectResult lookup(User user, String key)
                    {
                        return projectService.getProjectByKey(user, key);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final Project project)
    {
        return ImmutableMap.<String, Object>of(PROJECT_FIELD_NAME, ImmutableMap.of(
                ID_FIELD_NAME, project.getId(),
                KEY_FIELD_NAME, project.getKey()
        ));
    }

}
