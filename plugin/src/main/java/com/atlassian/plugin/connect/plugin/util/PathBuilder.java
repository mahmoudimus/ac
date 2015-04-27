package com.atlassian.plugin.connect.plugin.util;

import org.apache.commons.lang3.StringUtils;

/**
 * A Utility class to concatenate path fragments without having to worry  about trailing and leading slashes.
 * This class just takes care of path separators, it doesn't assume or change anything related to URL or Filename encoding.
 * Trailing slashes at the end of the path are preserved.
 *
 * All of the following calls lead to the same result, http://example.com/path.
 *
 * new PathBuilder("http://example.com").withPathFragment("/path").build()
 * new PathBuilder("http://example.com/").withPathFragment("/path").build()
 * new PathBuilder("http://example.com/").withPathFragment("path").build()
 * new PathBuilder("http://example.com").withPathFragment("path").build()
 */
public class PathBuilder
{
    private StringBuilder path = new StringBuilder();

    public PathBuilder()
    {
        this.path = new StringBuilder();
    }

    public PathBuilder(String baseUrl)
    {
        this.path = new StringBuilder(baseUrl);
    }

    /**
     * Appends a path segment, ensuring that there is only one path separator
     * between the existing path and the new segment
     * @param fragment A path fragment, i.e. one or multiple path segments.
     *                 Null fragments are ignored and don't change the path at all.
     *                 Empty path segments ('', '/') are represented as empty path segments (i.e. multiple adjoining separators).
     * @return the PathBuilder instance
     */
    public PathBuilder withPathFragment(String fragment)
    {
        if (null != fragment)
        {
            if (!StringUtils.endsWith(path, "/") || "".equals(fragment) || "/".equals(fragment))
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
        }
        return this;
    }

    public String build()
    {
        return path.toString();
    }
}
