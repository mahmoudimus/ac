package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);


    private PluggableParametersExtractor pluggableParametersExtractor;
    private final RequestJsonParameterUtil requestJsonParameterUtil = new RequestJsonParameterUtil();

    @Autowired
    public ModuleContextParserImpl(PluggableParametersExtractor pluggableParametersExtractor)
    {
        this.pluggableParametersExtractor = pluggableParametersExtractor;
    }

    @Override
    public Map<String, String> parseContextParameters(final HttpServletRequest req)
    {
        Map<String, String> requestContextParameters = new HashMap<>();
        final Map<String, String[]> parameterMap = requestJsonParameterUtil.tryExtractContextFromJson(req.getParameterMap());
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
        {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values.length > 1)
            {
                log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                        + "(key was " + key + ")");
            }
            requestContextParameters.put(key, values[0]);
        }
        Map<String, String> contextParameters = pluggableParametersExtractor.getParametersAccessibleByCurrentUser(requestContextParameters);
        return contextParameters;
    }
}
