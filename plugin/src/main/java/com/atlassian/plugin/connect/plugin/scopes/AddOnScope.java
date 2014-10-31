package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.scope.ApiScope;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class AddOnScope implements ApiScope, Comparable<AddOnScope>
{
    private final String key;
    private final Iterable<AddOnScopeApiPath> paths;
    private transient final Iterable<ApiResourceInfo> apiResourceInfos;

    public AddOnScope(String key, Iterable<AddOnScopeApiPath> paths)
    {
        this.key = checkNotNull(key);
        this.paths = checkNotNull(paths);
        this.apiResourceInfos = concat(transform(paths, new Function<AddOnScopeApiPath, Iterable<ApiResourceInfo>>()
        {
            @Override
            public Iterable<ApiResourceInfo> apply(@Nullable AddOnScopeApiPath path)
            {
                return null == path ? Collections.<ApiResourceInfo>emptySet() : path.getApiResourceInfos();
            }
        }));
    }

    @Override
    public boolean allow(final HttpServletRequest request, final @Nullable UserKey user)
    {
        return any(paths, new Predicate<AddOnScopeApiPath>()
        {
            @Override
            public boolean apply(@Nullable AddOnScopeApiPath path)
            {
                return null != path && path.allow(request, user);
            }
        });
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfos;
    }

    public Iterable<AddOnScopeApiPath> getPaths()
    {
        return paths;
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        // don't consider apiResourceInfo because they are a static transform of paths
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("key", getKey())
                .append("paths", paths)
                .toString();
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

        AddOnScope that = (AddOnScope) o;
        // don't consider apiResourceInfo because they are a static transform of paths
        return new EqualsBuilder()
                .append(key, that.key)
                .append(paths, that.paths)
                .build();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(29, 7)
                // don't consider apiResourceInfo because they are a static transform of paths
                .appendSuper(super.hashCode())
                .append(key)
                .append(paths)
                .build();
    }

    @Override
    public int compareTo(AddOnScope o)
    {
        return null == o ? 1 : ScopeName.valueOf(getKey()).compareTo(ScopeName.valueOf(o.getKey()));
    }
}
