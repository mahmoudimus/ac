package com.atlassian.plugin.connect.plugin.util;

import org.apache.commons.lang3.StringUtils;

public class PathBuilder
{
    private StringBuilder path = new StringBuilder();

    public PathBuilder withBaseUrl(String baseUrl)
    {
        path = new StringBuilder(baseUrl);
        return this;
    }

    public PathBuilder withPathFragment(String fragment)
    {
        if (!StringUtils.endsWith(path, "/"))
        {
            path.append('/');
        }
        if (StringUtils.startsWith(fragment, "/"))
        {
            path.append(fragment, 1, fragment.length());
        }
        else
        {
            path.append(fragment);
        }
        return this;
    }

    public String build()
    {
        return path.toString();
    }
}
