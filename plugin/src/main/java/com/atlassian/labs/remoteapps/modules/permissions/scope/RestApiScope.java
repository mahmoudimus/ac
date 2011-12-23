package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * An api scope implementation for REST resources
 */
public class RestApiScope implements ApiScope
{
    private final Iterable<RestScope> scopes;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    private static final Function<String,String> LOWERCASE_TRANSFORM = new Function<String,String>()
    {
        @Override
        public String apply(String from)
        {
            return from.toLowerCase(Locale.US);
        }
    };

    public RestApiScope(Iterable<RestScope> scopes)
    {
        this.scopes = scopes;
        this.apiResourceInfo = concat(transform(scopes, new Function<RestScope, Iterable<ApiResourceInfo>>()
        {
            @Override
            public Iterable<ApiResourceInfo> apply(RestScope from)
            {
                return from.getApiResourceInfo();
            }
        }));
    }

    public boolean allow(HttpServletRequest request, String user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        final String[] elements = StringUtils.split(pathInfo, '/');
        if (elements.length > 2 && "rest".equals(elements[0]))
        {
            String name = elements[1];
            String version = elements[2].toLowerCase(Locale.US);
            String method = request.getMethod().toLowerCase(Locale.US);
            String path = StringUtils.join(Arrays.copyOfRange(elements, 3, elements.length), "/");
            if (!path.startsWith("/"))
            {
                path = "/" + path;
            }

            for (RestScope scope : scopes)
            {
                if (scope.match(name, version, path, method))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    public static class RestScope
    {
        private final String name;
        private final Collection<String> versions;
        private final String basePath;
        private final Collection<String> methods;

        public RestScope(String name, Collection<String> versions, String basePath, Collection<String> methods)
        {
            this.name = name;
            this.versions = Collections2.transform(versions, LOWERCASE_TRANSFORM);
            this.basePath = basePath;
            this.methods = Collections2.transform(methods, LOWERCASE_TRANSFORM);
        }

        public String getName()
        {
            return name;
        }

        public boolean match(String name, String version, String path, String method)
        {
            return this.name.equalsIgnoreCase(name) &&
                this.versions.contains(version) &&
                path.startsWith(basePath) &&
                this.methods.contains(method);
        }

        public Iterable<ApiResourceInfo> getApiResourceInfo()
        {
            List<ApiResourceInfo> infos = newArrayList();
            for (String version : versions)
            {
                for (String method : methods)
                {
                    StringBuilder sb = new StringBuilder("/rest/")
                            .append(name).append("/")
                            .append(version)
                            .append(basePath);
                    infos.add(new ApiResourceInfo(sb.toString(), method.toUpperCase(Locale.US)));
                }
            }
            return infos;
        }
    }
}
