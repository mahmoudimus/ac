package com.atlassian.plugin.connect;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public final class TestJiraContextParametersExtractor implements ContextParametersExtractor
{
    @Override
    public Map<String, String> extractParameters(final Map<String, Object> context)
    {
        Object projectObj = context.get("project");
        if (projectObj != null && projectObj instanceof Project)
        {
            Project project = (Project) projectObj;
            return ImmutableMap.of("project.keyConcatId", project.getKey() + project.getId());
        }
        else
        {
            return Collections.emptyMap();
        }
    }
}
