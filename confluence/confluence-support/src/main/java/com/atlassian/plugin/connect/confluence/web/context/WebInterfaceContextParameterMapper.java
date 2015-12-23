package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper.additionalValue;

@ConfluenceComponent
public class WebInterfaceContextParameterMapper implements TypeBasedConnectContextParameterMapper<WebInterfaceContext>
{

    private static final String CONTEXT_KEY = "webInterfaceContext";

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<WebInterfaceContext> getContextValueClass()
    {
        return WebInterfaceContext.class;
    }

    @Override
    public Set<AdditionalValue<WebInterfaceContext, ?>> getAdditionalContextValues()
    {
        Set<AdditionalValue<WebInterfaceContext, ?>> additionalValues = new HashSet<>();
        additionalValues.add(additionalValue(ConfluenceUser.class, WebInterfaceContext::getTargetedUser));
        additionalValues.add(additionalValue(Space.class, WebInterfaceContext::getSpace));
        additionalValues.add(additionalValue(AbstractPage.class, WebInterfaceContext::getPage));
        additionalValues.add(additionalValue(ContentEntityObject.class, WebInterfaceContext::getPage));
        return additionalValues;
    }

    @Override
    public Set<Parameter<WebInterfaceContext>> getParameters()
    {
        return Collections.emptySet();
    }
}
