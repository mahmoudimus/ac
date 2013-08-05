package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * Extracts project parameters that can be included in webpanel's iframe url.
 */
public class ProjectWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    private static final String PROJECT_CONTEXT_KEY = "project";

    @Override
    public Map<String, Object> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROJECT_CONTEXT_KEY))
        {
            Project project = (Project) context.get(PROJECT_CONTEXT_KEY);
            if (null != project)
            {
                return ImmutableMap.<String, Object>of("project", ImmutableMap.of(
                        "id", project.getId(),
                        "key", project.getKey()
                ));
            }
        }
        return Collections.emptyMap();
    }
}
