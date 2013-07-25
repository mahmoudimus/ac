package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Extracts project id that will be included in webpanel's iframe url.
 */
public class ProjectIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    private static final String PROJECT_CONTEXT_KEY = "project";

    @Override
    public void extract(final Map<String, Object> context, final Map<String, Object> whiteListedContext)
    {
        if (context.containsKey(PROJECT_CONTEXT_KEY))
        {
            Project project = (Project) context.get(PROJECT_CONTEXT_KEY);
            if (null != project)
            {
                whiteListedContext.put("project", ImmutableMap.of(
                        "id", project.getId(),
                        "key", project.getKey()
                ));
            }
        }
    }
}
