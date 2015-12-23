package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper.additionalValue;

@ConfluenceComponent
public class PageContextParameterMapper implements TypeBasedConnectContextParameterMapper<AbstractPage>
{

    private static final String CONTEXT_KEY = "page";

    private final Set<Parameter<AbstractPage>> parameters;

    @Autowired
    public PageContextParameterMapper(PageParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<AbstractPage> getContextValueClass()
    {
        return AbstractPage.class;
    }

    @Override
    public Set<AdditionalValue<AbstractPage, ?>> getAdditionalContextValues()
    {
        Set<AdditionalValue<AbstractPage, ?>> additionalValues = new HashSet<>();
        additionalValues.add(additionalValue(ContentEntityObject.class, (contextValue) -> contextValue));
        additionalValues.add(additionalValue(Space.class, AbstractPage::getSpace));
        return additionalValues;
    }

    @Override
    public Set<Parameter<AbstractPage>> getParameters()
    {
        return parameters;
    }

    public static interface PageParameter extends ConnectContextParameterMapper.Parameter<AbstractPage>
    {}
}
