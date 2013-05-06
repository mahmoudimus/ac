package com.atlassian.plugin.remotable.plugin.util.contextparameter;

import com.atlassian.plugin.remotable.plugin.util.node.Node;
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
    public RequestContextParameterFactory parseContextParameters(Node entity)
    {
        Set<String> queryParams = newHashSet();
        Set<String> headerParams = newHashSet();
        if (entity.get("context-parameters").exists())
        {
            for (Node e : entity.get("context-parameters").getChildren("context-parameter"))
            {
                if ("header".equals(e.get("type").asString("query")))
                {
                    headerParams.add(e.get("name").asString());
                }
                else
                {
                    queryParams.add(e.get("name").asString());
                }
            }
        }
        return new RequestContextParameterFactory(queryParams, headerParams);
    }
}
