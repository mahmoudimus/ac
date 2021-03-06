package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.api.util.ServletUtils;
import com.atlassian.plugin.connect.plugin.auth.scope.ApiResourceInfo;

import com.google.common.base.Function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.dom4j.Document;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.api.util.Dom4jUtils.readDocument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;

/**
 * An api scope for SOAP requests
 */
public final class RpcEncodedSoapApiScopeHelper {
    private final String path;
    private final Collection<SoapScope> soapActions;
    private final String httpMethod;
    private transient final Iterable<ApiResourceInfo> apiResourceInfo;

    public RpcEncodedSoapApiScopeHelper(final String path, Collection<SoapScope> soapActions, String httpMethod) {
        this.path = path;
        this.soapActions = soapActions;
        this.httpMethod = checkNotNull(httpMethod).toUpperCase();
        this.apiResourceInfo = transform(soapActions, from -> new ApiResourceInfo(path, RpcEncodedSoapApiScopeHelper.this.httpMethod, from.name));
    }

    public RpcEncodedSoapApiScopeHelper(String path, final String namespace,
                                        Collection<String> methods,
                                        String httpMethod) {
        // convert to ArrayList because the Collection subclass returned from Collections2.transform() has a broken equals()
        this(path, new ArrayList<>(transform(methods, from -> new SoapScope(namespace, from))), httpMethod);
    }

    public static Optional<Pair<String, String>> getMethod(HttpServletRequest rq) {
        Document doc = readDocument(rq);
        if (null == doc) {
            return Optional.empty();
        }
        Element root = doc.getRootElement();
        if (null == root) {
            return Optional.empty();
        }
        Element body = root.element("Body");
        if (null == body) {
            return Optional.empty();
        }
        Element methodElement = (Element) body.elements().get(0);
        if (null == methodElement) {
            return Optional.empty();
        }
        String name = methodElement.getName();
        String namespace = methodElement.getNamespaceURI();
        return Optional.of(Pair.pair(namespace, name));
    }

    public boolean allow(HttpServletRequest request) {
        if (!httpMethod.equals(request.getMethod())) {
            return false;
        }

        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo)) {
            Optional<Pair<String, String>> maybeNamespaceAndName = getMethod(request);
            if (!maybeNamespaceAndName.isPresent()) {
                return false;
            }
            Pair<String, String> namespaceAndName = maybeNamespaceAndName.get();
            for (SoapScope scope : soapActions) {
                if (scope.match(namespaceAndName.left(), namespaceAndName.right())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos() {
        return apiResourceInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RpcEncodedSoapApiScopeHelper that = (RpcEncodedSoapApiScopeHelper) o;
        // don't consider apiResourceInfo because they are a static transform of soapActions and path
        return new EqualsBuilder()
                .append(path, that.path)
                .append(soapActions, that.soapActions)
                .build();
    }

    @Override
    public int hashCode() {
        // don't consider apiResourceInfo because they are a static transform of soapActions and path
        return new HashCodeBuilder(41, 5)
                .append(path)
                .append(soapActions)
                .build();
    }

    @Override
    public String toString() {
        // don't consider apiResourceInfo because they are a static transform of soapActions and path
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("path", path)
                .append("soapActions", soapActions)
                .build();
    }

    public static class SoapScope {
        private final String namespace;
        private final String name;

        public SoapScope(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        public boolean match(String namespace, String name) {
            return this.namespace.equals(namespace) && this.name.equals(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SoapScope that = (SoapScope) o;
            return new EqualsBuilder()
                    .append(name, that.name)
                    .append(namespace, that.namespace)
                    .build();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(37, 11)
                    .append(name)
                    .append(namespace)
                    .build();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("name", name)
                    .append("namespace", namespace)
                    .build();
        }
    }
}
