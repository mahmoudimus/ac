package com.atlassian.plugin.connect.plugin.util.contextparameter;

import java.util.List;
import java.util.Set;

import com.atlassian.plugin.connect.api.util.RequestContextParameterFactory;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Parses a configuration entity for context parameters
 */
@Component
public class ContextParameterParser
{
    public RequestContextParameterFactory parseContextParameters(Element entity)
    {
        Set<String> queryParams = newHashSet();
        Set<String> headerParams = newHashSet();
        if (entity.element("context-parameters") != null)
        {
            for (Element e : (List<Element>) entity.element("context-parameters").elements())
            {
                if ("header".equals(getOptionalAttribute(e, "type", "query")))
                {
                    headerParams.add(e.attributeValue("name"));
                }
                else
                {
                    queryParams.add(e.attributeValue("name"));
                }
            }
        }
        return new RequestContextParameterFactory(queryParams, headerParams);
    }
}
