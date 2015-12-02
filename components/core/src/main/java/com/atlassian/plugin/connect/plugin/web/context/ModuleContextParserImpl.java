package com.atlassian.plugin.connect.plugin.web.context;

import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);


    private final ModuleContextFilter moduleContextFilter;
    private final RequestJsonParameterUtil requestJsonParameterUtil = new RequestJsonParameterUtil();

    @Autowired
    public ModuleContextParserImpl(ModuleContextFilter moduleContextFilter)
    {
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public Map<String, String> parseContextParameters(final HttpServletRequest req)
    {
        Map<String, String> unfiltered = new HashMapModuleContextParameters();
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
            unfiltered.put(key, values[0]);
        }
        return moduleContextFilter.filter(unfiltered);
    }


}
