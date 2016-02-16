package com.atlassian.plugin.connect.plugin.web.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);

    private final ModuleContextFilter moduleContextFilter;
    private final WebFragmentModuleContextExtractor contextExtractor;

    private final RequestJsonParameterUtil requestJsonParameterUtil = new RequestJsonParameterUtil();

    @Autowired
    public ModuleContextParserImpl(ModuleContextFilter moduleContextFilter, final WebFragmentModuleContextExtractor contextExtractor)
    {
        this.moduleContextFilter = moduleContextFilter;
        this.contextExtractor = contextExtractor;
    }

    @Override
    public ModuleContextParameters parseContextParameters(final HttpServletRequest req)
    {
        Map<String, String> queryParams = new HashMap<>();
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
            queryParams.put(key, values[0]);
        }

        ModuleContextParameters unfiltered = new HashMapModuleContextParameters(Collections.emptyMap());
        unfiltered.putAll(queryParams);

        ModuleContextParameters filtered = moduleContextFilter.filter(unfiltered);

        ModuleContextParameters filteredWithProductContext = new HashMapModuleContextParameters(contextExtractor.reverseExtraction(req, filtered));
        filteredWithProductContext.putAll(filtered);

        return filteredWithProductContext;
    }
}
