package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.connect.plugin.auth.scope.ApiResourceInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public interface AddonScopeApiPath {
    boolean allow(HttpServletRequest request);

    Iterable<ApiResourceInfo> getApiResourceInfos();

    void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources,
               Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources,
               Collection<PathScopeHelper> paths);

    class RestApiPath implements AddonScopeApiPath {
        private final Collection<RestApiScopeHelper.RestScope> resources;
        private final RestApiScopeHelper restApiScopeHelper;

        @VisibleForTesting
        public RestApiPath(Collection<RestApiScopeHelper.RestScope> resources) {
            this.resources = resources;
            this.restApiScopeHelper = new RestApiScopeHelper(Preconditions.checkNotNull(resources));
        }

        @Override
        public boolean allow(HttpServletRequest request) {
            return restApiScopeHelper.allow(request);
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos() {
            return restApiScopeHelper.getApiResourceInfos();
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources,
                          Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources,
                          Collection<PathScopeHelper> paths) {
            restResources.addAll(this.resources);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RestApiPath that = (RestApiPath) o;
            return new EqualsBuilder()
                    .append(resources, that.resources)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(23, 31)
                    .append(resources)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("resources", resources)
                    .toString();
        }
    }

    class SoapRpcApiPath implements AddonScopeApiPath {
        private final Collection<RpcEncodedSoapApiScopeHelper> soapRpcResources;

        public SoapRpcApiPath(Collection<RpcEncodedSoapApiScopeHelper> soapRpcResources) {
            this.soapRpcResources = soapRpcResources;
        }

        @Override
        public boolean allow(HttpServletRequest request) {
            for (RpcEncodedSoapApiScopeHelper soapRpcResource : soapRpcResources) {
                if (soapRpcResource.allow(request)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos() {
            return concat(transform(soapRpcResources,
                    input -> null == input ? Collections.<ApiResourceInfo>emptySet() : input.getApiResourceInfos()));
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources,
                          Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources,
                          Collection<PathScopeHelper> paths) {
            soapResources.addAll(this.soapRpcResources);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SoapRpcApiPath that = (SoapRpcApiPath) o;
            return new EqualsBuilder()
                    .append(soapRpcResources, that.soapRpcResources)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return soapRpcResources != null ? soapRpcResources.hashCode() : 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("soapRpcResources", soapRpcResources)
                    .toString();
        }
    }

    class JsonRpcApiPath implements AddonScopeApiPath {
        private Collection<JsonRpcApiScopeHelper> jsonRpcResources;

        public JsonRpcApiPath(Collection<JsonRpcApiScopeHelper> jsonRpcResources) {
            this.jsonRpcResources = jsonRpcResources;
        }

        @Override
        public boolean allow(HttpServletRequest request) {
            for (JsonRpcApiScopeHelper jsonResource : jsonRpcResources) {
                if (jsonResource.allow(request)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos() {
            return concat(transform(jsonRpcResources,
                    input -> null == input ? Collections.<ApiResourceInfo>emptySet() : input.getApiResourceInfos()));
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources,
                          Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources,
                          Collection<PathScopeHelper> paths) {
            jsonResources.addAll(this.jsonRpcResources);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            JsonRpcApiPath that = (JsonRpcApiPath) o;
            return new EqualsBuilder()
                    .append(jsonRpcResources, that.jsonRpcResources)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return jsonRpcResources != null ? jsonRpcResources.hashCode() : 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("jsonRpcResources", jsonRpcResources)
                    .toString();
        }
    }

    class XmlRpcApiPath implements AddonScopeApiPath {
        private Collection<XmlRpcApiScopeHelper> xmlRpcResources;

        public XmlRpcApiPath(Collection<XmlRpcApiScopeHelper> xmlRpcResources) {
            this.xmlRpcResources = xmlRpcResources;
        }

        @Override
        public boolean allow(HttpServletRequest request) {
            for (XmlRpcApiScopeHelper xmlResource : xmlRpcResources) {
                if (xmlResource.allow(request)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos() {
            return concat(transform(xmlRpcResources,
                    input -> null == input ? Collections.<ApiResourceInfo>emptySet() : input.getApiResourceInfos()));
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources,
                          Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources,
                          Collection<PathScopeHelper> paths) {
            xmlResources.addAll(this.xmlRpcResources);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            XmlRpcApiPath that = (XmlRpcApiPath) o;
            return new EqualsBuilder()
                    .append(xmlRpcResources, that.xmlRpcResources)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return xmlRpcResources != null ? xmlRpcResources.hashCode() : 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("xmlRpcResources", xmlRpcResources)
                    .toString();
        }
    }

    class ApiPath implements AddonScopeApiPath {
        private final Collection<PathScopeHelper> paths;

        public ApiPath(Collection<PathScopeHelper> paths) {
            this.paths = paths;
        }

        @Override
        public boolean allow(final HttpServletRequest request) {
            return any(paths, path -> null != path && path.allow(request));
        }

        @Override
        public Iterable<ApiResourceInfo> getApiResourceInfos() {
            return concat(transform(paths,
                    input -> null == input ? Collections.<ApiResourceInfo>emptySet() : input.getApiResourceInfos()));
        }

        @Override
        public void addTo(Collection<RestApiScopeHelper.RestScope> restResources, Collection<RpcEncodedSoapApiScopeHelper> soapResources, Collection<JsonRpcApiScopeHelper> jsonResources, Collection<XmlRpcApiScopeHelper> xmlResources, Collection<PathScopeHelper> paths) {
            paths.addAll(this.paths);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ApiPath that = (ApiPath) o;
            return new EqualsBuilder()
                    .append(paths, that.paths)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return paths != null ? paths.hashCode() : 0;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("paths", paths)
                    .toString();
        }
    }
}
