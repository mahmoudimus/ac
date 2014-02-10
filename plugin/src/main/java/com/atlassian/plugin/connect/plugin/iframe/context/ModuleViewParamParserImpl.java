package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModuleViewParamParserImpl implements ModuleViewParamParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleViewParamParserImpl.class);

    @Override
    public ModuleViewParameters parseViewParameters(HttpServletRequest req)
    {
        Map<String, String[]> parameterMap = req.getParameterMap();
        String width = getParam(parameterMap, "width");
        String height = getParam(parameterMap, "height");
        return new ModuleViewParameters(width, height);
    }

    private String getParam(Map<String, String[]> parameterMap, String key)
    {
        String[] values = parameterMap.get(key);
        if (values.length > 1) {
            log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                    + "(key was " + key + ")");
        }
        return values[0];
    }
}
