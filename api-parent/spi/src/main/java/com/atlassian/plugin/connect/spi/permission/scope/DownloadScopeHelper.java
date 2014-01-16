package com.atlassian.plugin.connect.spi.permission.scope;

import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

/**
 * Download scope for GET requests on paths that start with a certain prefix
 */
public final class DownloadScopeHelper
{
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private final Iterable<String> pathPrefixes;

    public DownloadScopeHelper(final String... pathPrefixes)
    {
        this.pathPrefixes = Lists.newArrayList(pathPrefixes);
        this.apiResourceInfo = transform(asList(pathPrefixes), new Function<String, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(from, "GET");
            }
        });
    }

    public boolean allow(final HttpServletRequest request, UserKey user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        return Iterables.any(pathPrefixes, new Predicate<String>()
        {
            @Override
            public boolean apply(final String prefix)
            {
                return pathInfo.startsWith(prefix) && "GET".equalsIgnoreCase(request.getMethod());
            }
        });
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
