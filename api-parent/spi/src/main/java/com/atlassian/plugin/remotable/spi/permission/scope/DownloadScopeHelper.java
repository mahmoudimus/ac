package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.spi.util.ServletUtils;
import com.google.common.base.Function;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

/**
 * Download scope for GET requests on paths that start with a certain prefix
 */
public final class DownloadScopeHelper
{
    private final Iterable<ApiResourceInfo> apiResourceInfo;
    private final String[] pathPrefixes;

    public DownloadScopeHelper(final String... pathPrefixes) {
        this.pathPrefixes = pathPrefixes;
        this.apiResourceInfo = transform(asList(pathPrefixes), new Function<String,ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(from, "GET");
            }
        });
    }

    public boolean allow(HttpServletRequest request, String user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        for (String prefix : pathPrefixes)
        {
            if (pathInfo.startsWith(prefix))
            {
                return true;
            }
        }
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
