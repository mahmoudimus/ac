package com.atlassian.plugin.connect.plugin.web.iframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class ModuleUiParamParserImpl implements ModuleUiParamParser {
    private static final Logger log = LoggerFactory.getLogger(ModuleUiParamParserImpl.class);

    @Override
    @SuppressWarnings("unchecked")
    public Optional<String> parseUiParameters(HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        String uiParams = getParam(parameterMap, "ui-params");
        return Optional.ofNullable(uiParams);
    }

    private String getParam(Map<String, String[]> parameterMap, String key) {
        String[] values = parameterMap.get(key);
        if (values != null && values.length > 1) {
            log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                    + "(key was " + key + ")");
        }
        return values == null ? null : values[0];
    }
}
