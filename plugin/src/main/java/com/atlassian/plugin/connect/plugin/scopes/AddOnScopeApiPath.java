package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.RpcEncodedSoapApiScopeHelper;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public interface AddOnScopeApiPath
{
    boolean allow(HttpServletRequest request, @Nullable UserKey user);
    Iterable<ApiResourceInfo> getApiResourceInfos();

    void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources);

    static class RestApiPath implements AddOnScopeApiPath
    {
        private final Collection<RestApiScopeHelper.RestScope> resources;
        private final RestApiScopeHelper restApiScopeHelper;

        RestApiPath(Collection<RestApiScopeHelper.RestScope> resources)
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
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources)
        {
            restResources.addAll(this.resources);
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

    public class SoapRpcApiPath implements AddOnScopeApiPath
    {
        private Collection<RpcEncodedSoapApiScopeHelper> soapRpcResources;

        public SoapRpcApiPath(Collection<RpcEncodedSoapApiScopeHelper> soapRpcResources)
        {
            this.soapRpcResources = soapRpcResources;
        }

        @Override
        public boolean allow(HttpServletRequest request, @Nullable UserKey user)
        {
            for (RpcEncodedSoapApiScopeHelper soapRpcResource : soapRpcResources)
            {
                if (soapRpcResource.allow(request, user))
                {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos()
        {
            return concat(transform(soapRpcResources, new Function<RpcEncodedSoapApiScopeHelper, Iterable<ApiResourceInfo>>()
            {
                @Override
                public Iterable<ApiResourceInfo> apply(@Nullable RpcEncodedSoapApiScopeHelper input)
                {
                    return null == input ? Collections.<ApiResourceInfo>emptySet() : input.getApiResourceInfos();
                }
            }));
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources)
        {
            soapResources.addAll(this.soapRpcResources);
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

            SoapRpcApiPath that = (SoapRpcApiPath) o;
            return new EqualsBuilder()
                    .append(soapRpcResources, that.soapRpcResources)
                    .isEquals();
        }

        @Override
        public int hashCode()
        {
            return soapRpcResources != null ? soapRpcResources.hashCode() : 0;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("soapRpcResources", soapRpcResources)
                    .toString();
        }
    }
}