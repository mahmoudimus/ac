package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.project.Project;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestContextBuilder
{
    public static final String PROJECT_KEY = "KEY";
    public static final long PROJECT_ID = 1234L;

    public static Map<String, Object> build()
    {
        Project project = mock(Project.class);
        when(project.getKey()).thenReturn(PROJECT_KEY);
        when(project.getId()).thenReturn(PROJECT_ID);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("project", project);

        return context;
    }
}
