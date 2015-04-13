package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
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

/**
 * Substitutes strings with variables defined with those defined in a given context.
 * <p>
 * Variables are in the form {var.name}, and are looked up in a nested map.
 * <p>
 * For example, given the source string "hi={user.name}" and a context such as
 * createMapOf("user", createMapOf("name", "joe")), {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi=joe".
 * <p>
 * All values in the context are percent-encoded for subsitution into a URL.
 * <p>
 * Variables that that cannot be found in the map are replaced by an empty string. For example,
 * given the source String "hi={foo.bar}" and an empty map, {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi="
 */
@Component
public class UrlVariableSubstitutor
{
    private static final Logger log = LoggerFactory.getLogger(UrlVariableSubstitutor.class);

    // in "http://server/path?foo={var}&something" match "{var}" with group 1 = "var"
    // or legacy case
    // in "http://server/path?foo=${var}&something" match "${var}" with group 1 = "var"
    public static final String PLACEHOLDER_PATTERN_STRING = "\\$?\\{([^}]*)}";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_PATTERN_STRING);

    // in "http://server/path?name={var}&something" match "name={var}" with groups = "name", "{var}" and "var"
    private static final Pattern VARIABLE_EQUALS_PLACEHOLDER_PATTERN = Pattern.compile("([^}&?]+)=(" + PLACEHOLDER_PATTERN_STRING + ")");
    private final IsDevModeService devModeService;

    @Autowired
    public UrlVariableSubstitutor(IsDevModeService devModeService)
    {
        this.devModeService = checkNotNull(devModeService);
    }

    /**
     * Replaces all variables in the given source with values from the given context.
     * @param source string containing variables
     * @param context context containing values to replace
     * @return source with variables replaced by values.
     */
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

    /**
     * Appends (rather than substitutes) a map of parameters to the end of the url.
     *
     * @param source the original URL
     * @param parameters the parameters to append
     * @return the URL, with the supplied parameters appended
     */
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

    /**
     * Parses from the given URL a {@link Map} of name-in-source to context-variable-name.
     * @param source string containing variables (e.g. "http://server:80/path?my_page_id={page.id}" or "my_page_id={page.id}")
     * @return {@link Map} of name-in-source to context-variable-name (e.g. "my_page_id" => "page.id")
     */
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
