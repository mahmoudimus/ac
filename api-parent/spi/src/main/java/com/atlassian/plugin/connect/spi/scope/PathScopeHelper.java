package com.atlassian.plugin.connect.spi.scope;

import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * Download scope for GET requests on paths that start with a certain prefix
 */
public final class PathScopeHelper
{
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private final Iterable<String> paths;
    private final boolean isRegex;
    private final String httpMethod;

    public PathScopeHelper(final boolean isRegex, final Collection<String> paths)
    {
        this(isRegex, paths, "GET");
    }

    public PathScopeHelper(final boolean isRegex, final Collection<String> paths, String httpMethod)
    {
        this.paths = checkNotNull(paths);
        this.isRegex = isRegex;
        this.httpMethod = checkNotNull(httpMethod);
        this.apiResourceInfo = transform(paths, new Function<String, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(from, PathScopeHelper.this.httpMethod);
            }
        });
    }

    public PathScopeHelper(final boolean isRegex, final String... paths)
    {
        this(isRegex, Lists.newArrayList(paths));
    }

    public boolean allow(final HttpServletRequest request, UserKey user)
    {
        if (!this.httpMethod.equalsIgnoreCase(request.getMethod()))
        {
            return false;
        }

        final String pathInfo = ServletUtils.extractPathInfo(request);
        return Iterables.any(paths, new Predicate<String>()
        {
            @Override
            public boolean apply(final String path)
            {
                return isRegex
                    ? pathInfo.matches(path)
                    : pathInfo.startsWith(path);
            }
        });
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
