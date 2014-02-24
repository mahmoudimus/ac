package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);


    private final ModuleContextFilter moduleContextFilter;
    private final ModuleContextJsonExtractor moduleContextJsonExtractor = new ModuleContextJsonExtractor();

    @Autowired
    public ModuleContextParserImpl(ModuleContextFilter moduleContextFilter)
    {
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public ModuleContextParameters parseContextParameters(final HttpServletRequest req)
    {
        ModuleContextParameters unfiltered = new HashMapModuleContextParameters();
        final Map<String, String[]> parameterMap = moduleContextJsonExtractor.tryExtractContextFromJson(req.getParameterMap());
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
