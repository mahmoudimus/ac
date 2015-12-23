package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@JiraComponent
public class ProjectContextParameterMapper implements TypeBasedConnectContextParameterMapper<Project>
{

    private static final String CONTEXT_KEY = "project";

    private Set<Parameter<Project>> parameters;

    @Autowired
    public ProjectContextParameterMapper(ProjectParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<Project> getContextValueClass()
    {
        return Project.class;
    }

    @Override
    public Set<Parameter<Project>> getParameters()
    {
        return parameters;
    }

    public static interface ProjectParameter extends ConnectContextParameterMapper.Parameter<Project>
    {}
}
