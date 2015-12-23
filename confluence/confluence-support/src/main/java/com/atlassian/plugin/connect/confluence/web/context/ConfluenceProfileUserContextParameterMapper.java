package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ConfluenceComponent
public class ConfluenceProfileUserContextParameterMapper implements TypeBasedConnectContextParameterMapper<ConfluenceUser>
{

    private static final String CONTEXT_KEY = "targetUser";

    private final Set<Parameter<ConfluenceUser>> parameters;

    @Autowired
    public ConfluenceProfileUserContextParameterMapper(UserParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<ConfluenceUser> getContextValueClass()
    {
        return ConfluenceUser.class;
    }

    @Override
    public Set<Parameter<ConfluenceUser>> getParameters()
    {
        return parameters;
    }

    public static interface UserParameter extends ConnectContextParameterMapper.Parameter<ConfluenceUser>
    {}
}
