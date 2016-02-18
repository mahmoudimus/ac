package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.api.util.ServletUtils;
import com.atlassian.plugin.connect.plugin.auth.scope.ApiResourceInfo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

/**
 * Download scope for GET requests on paths that start with a certain prefix
 */
public final class PathScopeHelper
{
    private final Collection<ApiResourceInfo> apiResourceInfo;
    private final Collection<String> paths;
    private final boolean isRegex;
    private final String httpMethod;

    public PathScopeHelper(final boolean isRegex, final String path)
    {
        this(isRegex, singletonList(checkNotNull(path)), "GET");
    }

    public PathScopeHelper(final boolean isRegex, final Collection<String> paths, String httpMethod)
    {
        this.paths = checkNotNull(paths);
        this.isRegex = isRegex;
        this.httpMethod = checkNotNull(httpMethod);
        this.apiResourceInfo = paths.stream()
                .map(from -> new ApiResourceInfo(from, PathScopeHelper.this.httpMethod))
                .collect(Collectors.toList());
    }

    public boolean allow(final HttpServletRequest request)
    {
        if (!this.httpMethod.equalsIgnoreCase(request.getMethod()))
        {
            return false;
        }

        final String pathInfo = ServletUtils.extractPathInfo(request);
        return paths.stream().anyMatch(path -> isRegex
                ? pathInfo.matches(path)
                : pathInfo.startsWith(path));
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PathScopeHelper that = (PathScopeHelper) o;
        return new EqualsBuilder()
                .append(isRegex, that.isRegex)
                .append(paths, that.paths)
                .build();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(47, 19)
                .append(isRegex)
                .append(paths)
                .build();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("isRegex", isRegex)
                .append("paths", paths)
                .build();
    }
}
