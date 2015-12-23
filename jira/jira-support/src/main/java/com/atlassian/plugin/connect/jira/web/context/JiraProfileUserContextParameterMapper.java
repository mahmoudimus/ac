package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@JiraComponent
public class JiraProfileUserContextParameterMapper implements TypeBasedConnectContextParameterMapper<ApplicationUser>
{

    public static final String CONTEXT_KEY = "profileUser";

    private final Set<Parameter<ApplicationUser>> parameters;

    @Autowired
    public JiraProfileUserContextParameterMapper(UserParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<ApplicationUser> getContextValueClass()
    {
        return ApplicationUser.class;
    }

    @Override
    public Set<Parameter<ApplicationUser>> getParameters()
    {
        return parameters;
    }

    public static interface UserParameter extends ConnectContextParameterMapper.Parameter<ApplicationUser>
    {}
}
