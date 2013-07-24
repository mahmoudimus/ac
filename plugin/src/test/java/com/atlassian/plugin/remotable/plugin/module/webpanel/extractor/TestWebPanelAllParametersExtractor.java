package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.IssueIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProjectIdWebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestWebPanelAllParametersExtractor
{
    private static final long ISSUE_ID = 100l;

    @Test
    public void testGetExtractedWebPanelParameters()
    {
        final List<WebPanelParameterExtractor> parameterExtractors = newArrayList(
                new IssueIdWebPanelParameterExtractor(),
                new ProjectIdWebPanelParameterExtractor()
        );

        Issue issue = mock(Issue.class);
        when(issue.getId()).thenReturn(ISSUE_ID);

        final WebPanelURLParametersSerializer webPanelURLParametersSerializer = new WebPanelURLParametersSerializer(parameterExtractors);
        final Map<String, Object> context = ImmutableMap.<String, Object>builder()
                .put("issue", issue)
                .build();
        Map<String, Object> extractedWebPanelParameters = webPanelURLParametersSerializer.getExtractedWebPanelParameters(context);

        assertThat(extractedWebPanelParameters.keySet(), hasItem("issue"));
        Map<String, Object> issueMap = (Map<String, Object>) extractedWebPanelParameters.get("issue");
        assertThat(issueMap.keySet(), hasItem("id"));
        assertThat((Long) issueMap.get("id"), is(ISSUE_ID));
    }

}
