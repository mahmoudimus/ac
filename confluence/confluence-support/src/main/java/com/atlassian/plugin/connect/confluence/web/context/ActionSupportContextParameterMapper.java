package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@ConfluenceComponent
public class ActionSupportContextParameterMapper implements TypeBasedConnectContextParameterMapper<ConfluenceActionSupport>
{

    private static final String CONTEXT_KEY = "action";

    private WebInterfaceContextParameterMapper webInterfaceContextParameterMapper;

    @Autowired
    public ActionSupportContextParameterMapper(WebInterfaceContextParameterMapper webInterfaceContextParameterMapper)
    {
        this.webInterfaceContextParameterMapper = webInterfaceContextParameterMapper;
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<ConfluenceActionSupport> getContextValueClass()
    {
        return ConfluenceActionSupport.class;
    }

    @Override
    public Set<AdditionalValue<ConfluenceActionSupport, ?>> getAdditionalContextValues()
    {
        Set<AdditionalValue<ConfluenceActionSupport, ?>> additionalValues = new HashSet<>();
        for (AdditionalValue<WebInterfaceContext, ?> additionalValue : webInterfaceContextParameterMapper.getAdditionalContextValues())
        {
            Class contextValueClass = additionalValue.getContextValueClass();
            Function<ConfluenceActionSupport, Object> fallbackFunction = confluenceActionSupport
                    -> additionalValue.getValue(confluenceActionSupport.getWebInterfaceContext());
            additionalValues.add(TypeBasedConnectContextParameterMapper.additionalValue(contextValueClass, fallbackFunction));
        }
        return additionalValues;
    }

    @Override
    public Set<Parameter<ConfluenceActionSupport>> getParameters()
    {
        return Collections.emptySet();
    }
}
