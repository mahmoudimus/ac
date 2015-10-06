package com.atlassian.plugin.connect.spi.scope.helper;

import com.atlassian.plugin.connect.spi.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * An api scope implementation for REST resources
 */
public final class RestApiScopeHelper
{
    private final Iterable<RestScope> scopes;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    private static final Function<String, String> LOWERCASE_TRANSFORM = new Function<String, String>()
    {
        @Override
        public String apply(String from)
        {
            return from.toLowerCase(Locale.US);
        }
    };

    public RestApiScopeHelper(Iterable<RestScope> scopes)
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

    public boolean allow(HttpServletRequest request, UserKey user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        final String[] elements = StringUtils.split(pathInfo, '/');
        if (elements.length > 2 && "rest".equals(elements[0]))
        {
            String name = elements[1];
            String version = elements[2].toLowerCase(Locale.US);
            if (!isVersionString(version)) {
                version = null;
            }
            String method = request.getMethod().toLowerCase(Locale.US);
            String path = StringUtils.join(Arrays.copyOfRange(elements, version == null ? 2 : 3, elements.length), "/");
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

    @VisibleForTesting
    public static boolean isVersionString(String version)
    {
        return !StringUtils.isBlank(version) && ("latest".equalsIgnoreCase(version) || version.matches("\\d+([\\.-]\\w+)*"));
    }

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
        private final boolean pathIsRegex;

        public RestScope(String name, Collection<String> versions, String basePath, Collection<String> methods, boolean pathIsRegex)
        {
            this.name = name;
            this.versions = new ArrayList<>(Collections2.transform(versions, LOWERCASE_TRANSFORM)); // TransformedCollection.equals() is broken
            this.basePath = basePath;
            this.methods = new ArrayList<>(Collections2.transform(methods, LOWERCASE_TRANSFORM));
            this.pathIsRegex = pathIsRegex;
        }

        public String getName()
        {
            return name;
        }

        public boolean match(String name, String version, String path, String method)
        {
            boolean versionMatch = false;

            if ((this.versions.isEmpty() && version == null) || this.versions.contains(version)) {
                versionMatch = true;
            }

            return this.name.equalsIgnoreCase(name) &&
                    versionMatch &&
                    (pathIsRegex ? path.matches(basePath) : path.startsWith(basePath)) &&
                    this.methods.contains(method);
        }

        public Iterable<ApiResourceInfo> getApiResourceInfo()
        {
            List<ApiResourceInfo> infos = newArrayList();

            for (String method : methods)
            {
                if (versions.isEmpty())
                {
                    infos.add(new ApiResourceInfo("/rest/" + name + basePath, method.toUpperCase(Locale.US)));
                }
                else
                {
                    for (String version : versions)
                    {
                        infos.add(new ApiResourceInfo("/rest/" + name + "/" + version + basePath, method.toUpperCase(Locale.US)));
                    }
                }
            }
            return infos;
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

            RestScope restScope = (RestScope) o;
            return new EqualsBuilder()
                    .append(name, restScope.name)
                    .append(versions, restScope.versions)
                    .append(basePath, restScope.basePath)
                    .append(methods, restScope.methods)
                    .append(pathIsRegex, restScope.pathIsRegex)
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(29, 7)
                    .append(name)
                    .append(versions)
                    .append(basePath)
                    .append(methods)
                    .append(pathIsRegex)
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append(name)
                    .append(versions)
                    .append(basePath)
                    .append(methods)
                    .append(pathIsRegex)
                    .toString();
        }
    }
}
