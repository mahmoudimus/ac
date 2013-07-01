package com.atlassian.plugin.remotable.spi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Strings
{
    private Strings()
    {
    }

    public static String capitalize(String s)
    {
        if (s == null || s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1);
    }

    public static String decapitalize(String s)
    {
        if (s == null || s.length() == 0) return s;
        return s.substring(0, 1).toLowerCase(Locale.US) + s.substring(1);
    }

    public static String dasherize(String s)
    {
        if (s == null || s.length() == 0) return s;
        return replace(decapitalize(s), "([\\w\\d])([\\p{Upper}])", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return groups.get(1) + "-" + groups.get(2).toLowerCase(Locale.US);
            }
        });
    }

    public static String camelize(String s)
    {
        if (s == null || s.length() == 0) return s;
        return replace(decapitalize(s), "([\\w\\d])-([\\p{Lower}])", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return groups.get(1) + groups.get(2).toUpperCase(Locale.US);
            }
        });
    }

    public static String titleize(String s)
    {
        if (s == null || s.length() == 0) return s;
        return replace(capitalize(s), "([^\\s_\\-])[\\s_\\-]([^\\s_\\-])", new Replacer()
        {
            @Override
            public String replace(List<String> groups)
            {
                return groups.get(1) + " " + groups.get(2).toUpperCase(Locale.US);
            }
        });
    }

    public static String removeSuffix(String s, String suffix)
    {
        if (s == null || s.length() == 0) return s;
        if (s.endsWith(suffix)) s = s.substring(0, s.length() - suffix.length());
        return s;
    }

    public static String replace(String s, String patternStr, Replacer replacer)
    {
        if (s == null || s.length() == 0) return s;
        Pattern pattern = Pattern.compile(patternStr);
        Matcher m = pattern.matcher(s);
        StringBuffer buf = new StringBuffer(s.length());
        while (m.find())
        {
            int groupCount = m.groupCount();
            List<String> groups = new ArrayList<String>(groupCount);
            for (int i = 0; i <= groupCount; i++) groups.add(m.group(i));
            String text = replacer.replace(groups);
            m.appendReplacement(buf, Matcher.quoteReplacement(text));
        }
        m.appendTail(buf);
        return buf.toString();
    }

    public static interface Replacer
    {
        String replace(List<String> groups);
    }
}
