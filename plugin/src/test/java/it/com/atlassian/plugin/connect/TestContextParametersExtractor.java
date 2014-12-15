package it.com.atlassian.plugin.connect;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

public final class TestContextParametersExtractor implements ContextParametersExtractor
{
    @Override
    public Map<String, String> extractParameters(final Map<String, ? extends Object> context)
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
