package com.atlassian.labs.remoteapps.util.contextparameter;

import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Parses a configuration entity for context parameters
 */
@Component
public class ContextParameterParser
{
    private final UserManager userManager;

    @Autowired
    public ContextParameterParser(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public RequestContextParameterFactory parseContextParameters(Element entity)
    {
        if (entity.element("context-parameters") != null)
        {
            Set<String> queryParams = newHashSet();
            Set<String> headerParams = newHashSet();
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
            return new RequestContextParameterFactory(userManager, queryParams, headerParams);
        }
        return new RequestContextParameterFactory(userManager);
    }
}
