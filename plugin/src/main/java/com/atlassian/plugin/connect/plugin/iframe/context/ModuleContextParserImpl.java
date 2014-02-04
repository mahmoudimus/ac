package com.atlassian.plugin.connect.plugin.iframe.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);

    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public ModuleContextParserImpl(ModuleContextFilter moduleContextFilter)
    {
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public ModuleContextParameters parseContextParameters(final HttpServletRequest req)
    {
        ModuleContextParameters unfiltered = new HashMapModuleContextParameters();
        for (Object o : req.getParameterMap().entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            String[] values = (String[]) entry.getValue();
            if (values.length > 1) {
                log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                       + "(key was " + key + ")");
            }
            unfiltered.put(key, values[0]);
        }
        return moduleContextFilter.filter(unfiltered);
    }
}
