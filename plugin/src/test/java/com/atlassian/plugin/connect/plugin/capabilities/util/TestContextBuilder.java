package com.atlassian.plugin.connect.plugin.capabilities.util;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.project.Project;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestContextBuilder
{
    public static final String PROJECT_KEY = "KEY";
    public static final long PROJECT_ID = 1234L;

    public static Map<String, Object> buildContextMap()
    {
        return Collections.singletonMap("project", (Object)buildProject());
    }

    public static Project buildProject()
    {
        Project project = mock(Project.class);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(project.getId()).thenReturn(PROJECT_ID);
        return project;
    }

    public static BrowseComponentContext buildBrowseComponentContext()
    {
        Project project = buildProject();
        BrowseComponentContext browseContext = mock(BrowseComponentContext.class);
        when(browseContext.getProject()).thenReturn(project);
        when(browseContext.getComponent()).thenReturn(mock(ProjectComponent.class));
        return browseContext;
    }
}
