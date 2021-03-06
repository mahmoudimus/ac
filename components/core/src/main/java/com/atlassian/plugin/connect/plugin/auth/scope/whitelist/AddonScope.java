package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.plugin.auth.scope.ApiScope;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class AddonScope implements ApiScope, Comparable<AddonScope> {
    private final String key;
    private final Iterable<AddonScopeApiPath> paths;
    private transient final Iterable<ApiResourceInfo> apiResourceInfos;

    public AddonScope(String key, Iterable<AddonScopeApiPath> paths) {
        this.key = checkNotNull(key);
        this.paths = checkNotNull(paths);
        this.apiResourceInfos = concat(transform(paths,
                path -> null == path ? Collections.<ApiResourceInfo>emptySet() : path.getApiResourceInfos())
        );
    }

    @Override
    public boolean allow(final HttpServletRequest request) {
        return any(paths, path -> null != path && path.allow(request));
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos() {
        return apiResourceInfos;
    }

    public Iterable<AddonScopeApiPath> getPaths() {
        return paths;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        // don't consider apiResourceInfo because they are a static transform of paths
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("key", getKey())
                .append("paths", paths)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddonScope that = (AddonScope) o;
        // don't consider apiResourceInfo because they are a static transform of paths
        return new EqualsBuilder()
                .append(key, that.key)
                .append(paths, that.paths)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(29, 7)
                // don't consider apiResourceInfo because they are a static transform of paths
                .appendSuper(super.hashCode())
                .append(key)
                .append(paths)
                .build();
    }

    @Override
    public int compareTo(AddonScope o) {
        return null == o ? 1 : ScopeName.valueOf(getKey()).compareTo(ScopeName.valueOf(o.getKey()));
    }
}
