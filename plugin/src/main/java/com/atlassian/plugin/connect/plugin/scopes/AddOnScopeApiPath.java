package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import com.atlassian.sal.api.user.UserKey;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public interface AddOnScopeApiPath
{
    boolean allow(HttpServletRequest request, @Nullable UserKey user);
    Iterable<ApiResourceInfo> getApiResourceInfos();

    static class RestApiPath implements AddOnScopeApiPath
    {
        private final Collection<RestApiScopeHelper.RestScope> resources;
        private final RestApiScopeHelper restApiScopeHelper;

        @VisibleForTesting
        public RestApiPath(Collection<RestApiScopeHelper.RestScope> resources)
        {
            this.resources = resources;
            this.restApiScopeHelper = new RestApiScopeHelper(Preconditions.checkNotNull(resources));
        }

        @Override
        public boolean allow(HttpServletRequest request, @Nullable UserKey user)
        {
            return restApiScopeHelper.allow(request, user);
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos()
        {
            return restApiScopeHelper.getApiResourceInfos();
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

            RestApiPath that = (RestApiPath) o;
            return new EqualsBuilder()
                    .append(resources, that.resources)
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(23, 31)
                    .append(resources)
                    .toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("resources", resources)
                    .toString();
        }
    }
}