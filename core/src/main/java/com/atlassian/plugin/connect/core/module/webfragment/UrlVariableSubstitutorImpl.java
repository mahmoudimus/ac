package com.atlassian.plugin.connect.core.module.webfragment;

import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.service.IsDevModeService;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

@Component
public class UrlVariableSubstitutorImpl implements UrlVariableSubstitutor
{
    private static final Logger log = LoggerFactory.getLogger(UrlVariableSubstitutorImpl.class);

    // in "http://server/path?foo={var}&something" match "{var}" with group 1 = "var"
    // or legacy case
    // in "http://server/path?foo=${var}&something" match "${var}" with group 1 = "var"
    public static final String PLACEHOLDER_PATTERN_STRING = "\\$?\\{([^}]*)}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_PATTERN_STRING);

    // in "http://server/path?name={var}&something" match "name={var}" with groups = "name", "{var}" and "var"
    private static final Pattern VARIABLE_EQUALS_PLACEHOLDER_PATTERN = Pattern.compile("([^}&?]+)=(" + PLACEHOLDER_PATTERN_STRING + ")");
    private final IsDevModeService devModeService;

    @Autowired
    public UrlVariableSubstitutorImpl(IsDevModeService devModeService)
    {
        this.devModeService = checkNotNull(devModeService);
    }

    public String replace(String source, Map<String, ?> context)
    {
        if (devModeService.isDevMode() && source.contains("${"))
        {
            log.warn("Addon uses legacy variable format '${ variableName }' in url {}", new Object[] {source} );
        }

        Matcher m = PLACEHOLDER_PATTERN.matcher(source);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            String term = m.group(1);
            String value = fromContext(term, context);
            m.appendReplacement(sb, encodeQuery(value));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    public String append(String source, Map<String, String> parameters)
    {
        StringBuilder sb = new StringBuilder(source);
        String sep = source.contains("?") ? "&" : "?";
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            if (!StringUtils.isEmpty(entry.getValue()))
            {
                sb.append(sep).append(entry.getKey()).append("=").append(encodeQuery(entry.getValue()));
                sep = "&";
            }
        }
        return sb.toString();
    }

    public Map<String, String> getContextVariableMap(final String source)
    {
        Map<String, String> contextVariables = newHashMap();
        Matcher m = VARIABLE_EQUALS_PLACEHOLDER_PATTERN.matcher(source);
        while (m.find())
        {
            contextVariables.put(m.group(1), m.group(2));
        }
        return contextVariables;
    }

    private String encodeQuery(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "";
        }
        try
        {
            return URIUtil.encodeWithinQuery(value);
        }
        catch (URIException ex)
        {
            log.error("Error encoding value '" + value + "' to querystring", ex);
            return "";
        }
    }

    private String fromContext(String term, Map<String, ?> context)
    {
        Iterable<String> terms = Arrays.asList(term.split("\\."));
        Object value = fromContext(terms, context);
        if (null == value)
        {
            value = context.get(term);
        }
        if (null == value)
        {
            return "";
        }
        if (value instanceof Number
                || value instanceof String
                || value instanceof Boolean)
        {
            return value.toString();
        }
        else if (value instanceof String[]
                || value instanceof Number[]
                || value instanceof Boolean[])
        {
            return ((Object[]) value)[0].toString();
        }
        return "";
    }

    private Object fromContext(Iterable<String> terms, Map<String, ?> context)
    {
        Object current = context;
        for (String key : terms)
        {
            if (null == current)
            {
                return null;
            }
            if (current instanceof Map)
            {
                current = ((Map) current).get(key);
            }
        }
        return current;
    }
}
