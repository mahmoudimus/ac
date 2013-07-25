package com.atlassian.plugin.remotable.plugin.module.webfragment;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substitutes strings with variables defined with those defined in a given context.
 *
 */
@Component
public class StringSubstitutor
{
    private static final Logger log = LoggerFactory.getLogger(StringSubstitutor.class);

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]*)}");

    public String replace(String source, Object context)
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

    private String fromContext(String term, Object context)
    {
        Iterable<String> terms = Arrays.asList(term.split("\\."));
        Object value = fromContext(terms, context);
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
        return "";
    }

    private Object fromContext(Iterable<String> terms, Object context)
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
            else
            {
                BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(current);
                if (wrapper.isReadableProperty(key))
                {
                    current = wrapper.getPropertyValue(key);
                }
                else
                {
                    return null;
                }
            }
        }
        return current;
    }
}
