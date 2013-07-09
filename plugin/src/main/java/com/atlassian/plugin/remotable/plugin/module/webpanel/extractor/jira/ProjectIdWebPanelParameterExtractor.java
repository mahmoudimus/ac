package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * Extracts project id that will be included in webpanel's iframe url.
 */
public class ProjectIdWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    private static final String PROJECT_CONTEXT_KEY = "project";
    public static final String PROJECT_ID = "project_id";

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROJECT_CONTEXT_KEY))
        {
            Project project = (Project) context.get(PROJECT_CONTEXT_KEY);
            return Optional.<Map.Entry<String, String[]>>of(new ImmutableWebPanelParameterPair(PROJECT_ID, new String[] {String.valueOf(project.getId())}));
        }
        else
        {
            return Optional.absent();
        }
    }
}
