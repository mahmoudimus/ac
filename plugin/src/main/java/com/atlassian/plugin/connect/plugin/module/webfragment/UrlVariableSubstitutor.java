package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.google.common.collect.Sets;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substitutes strings with variables defined with those defined in a given context.
 * <p>
 * Variables are in the form ${var.name}, and are looked up in a nested map.
 * <p>
 * For example, given the source string "hi=${user.name}" and a context such as
 * createMapOf("user", createMapOf("name", "joe")), {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi=joe".
 * <p>
 * All values in the context are percent-encoded for subsitution into a URL.
 * <p>
 * Variables that that cannot be found in the map are replaced by an empty string. For example,
 * given the source String "hi=${foo.bar}" and an empty map, {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi="
 */
@Component
public class UrlVariableSubstitutor
{
    private static final Logger log = LoggerFactory.getLogger(UrlVariableSubstitutor.class);

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]*)}");

    /**
     * Replaces all variables in the given source with values from the given context.
     * @param source string containing variables
     * @param context context containing values to replace
     * @return source with variables replaced by values.
     */
    public String replace(String source, Map<String, Object> context)
    {
        Matcher m = PATTERN.matcher(source);
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
     * Returns a set of variables which are expected to be in the URL.
     * @param source string containing variables.
     */
    public Set<String> getContextVariables(final String source)
    {
        Set<String> contextVariables = Sets.newHashSet();
        Matcher m = PATTERN.matcher(source);
        while (m.find())
        {
            String term = m.group(1);
            contextVariables.add(term);
        }
        return contextVariables;
    }

    private String encodeQuery(String value)
    {
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

    private String fromContext(String term, Map<String, Object> context)
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

    private Object fromContext(Iterable<String> terms, Map<String, Object> context)
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
