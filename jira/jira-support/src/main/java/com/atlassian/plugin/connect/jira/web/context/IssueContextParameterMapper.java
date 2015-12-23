package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper.additionalValue;

@JiraComponent
public class IssueContextParameterMapper implements TypeBasedConnectContextParameterMapper<Issue>
{

    private static final String CONTEXT_KEY = "issue";

    private final Set<Parameter<Issue>> parameters;

    @Autowired
    public IssueContextParameterMapper(IssueParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<Issue> getContextValueClass()
    {
        return Issue.class;
    }

    @Override
    public Set<AdditionalValue<Issue, ?>> getAdditionalContextValues()
    {
        Set<AdditionalValue<Issue, ?>> additionalValues = new HashSet<>();
        additionalValues.add(additionalValue(Project.class, Issue::getProjectObject));
        return additionalValues;
    }

    @Override
    public Set<Parameter<Issue>> getParameters()
    {
        return parameters;
    }

    public static interface IssueParameter extends ConnectContextParameterMapper.Parameter<Issue>
    {}
}
