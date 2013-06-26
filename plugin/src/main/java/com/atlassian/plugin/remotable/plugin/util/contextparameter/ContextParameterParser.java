package com.atlassian.plugin.remotable.plugin.util.contextparameter;

import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
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
