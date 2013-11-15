package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.AbstractPermission;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.*;

public class AddOnScope extends AbstractPermission implements ApiScope
{
    private final Iterable<AddOnScopeApiPath> paths;
    private final Iterable<ApiResourceInfo> apiResourceInfos;

    public AddOnScope(String key, Iterable<AddOnScopeApiPath> paths)
    {
        super(checkNotNull(key));
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("key", getKey())
                .append("paths", paths)
                .toString();
    }
}