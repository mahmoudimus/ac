package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes ProjectComponent objects.
 */
public class ComponentSerializer extends AbstractJiraParameterSerializer<ProjectComponent, ProjectComponent>
{

    public static final String COMPONENT_FIELD_NAME = "component";

    public ComponentSerializer(final ProjectComponentService projectComponentService, UserManager userManager)
    {
        super(userManager, COMPONENT_FIELD_NAME,
                createNoopUnwrapper(ProjectComponent.class),
                new AbstractJiraIdParameterLookup<ProjectComponent>()
                {
                    @Override
                    public ProjectComponent lookup(User user, Long id)
                    {
                        return projectComponentService.find(user, null, id);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final ProjectComponent projectComponent)
    {
        return ImmutableMap.<String, Object>of(COMPONENT_FIELD_NAME,
                ImmutableMap.of(ID_FIELD_NAME, projectComponent.getId()));
    }

}
