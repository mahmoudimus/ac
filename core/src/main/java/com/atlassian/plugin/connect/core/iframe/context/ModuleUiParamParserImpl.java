package com.atlassian.plugin.connect.core.iframe.context;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import com.atlassian.fugue.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModuleUiParamParserImpl implements ModuleUiParamParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleUiParamParserImpl.class);

    @Override
    public Option<String> parseUiParameters(HttpServletRequest req)
    {
        Map<String, String[]> parameterMap = req.getParameterMap();
        String uiParams = getParam(parameterMap, "ui-params");
        return Option.option(uiParams);
    }

    private String getParam(Map<String, String[]> parameterMap, String key)
    {
        String[] values = parameterMap.get(key);
        if (values != null && values.length > 1) {
            log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                    + "(key was " + key + ")");
        }
        return values == null ? null : values[0];
    }
}
