package com.atlassian.plugin.connect.spi.permission.scope;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.spi.util.ServletUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.dom4j.Document;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.readDocument;
import static com.google.common.collect.Collections2.transform;

/**
 * An api scope for SOAP requests
 */
public final class RpcEncodedSoapApiScopeHelper
{
    private final String path;
    private final Collection<SoapScope> soapActions;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public RpcEncodedSoapApiScopeHelper(final String path, Collection<SoapScope> soapActions)
    {
        this.path = path;
        this.soapActions = soapActions;
        this.apiResourceInfo = Iterables.transform(soapActions, new Function<SoapScope, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(SoapScope from)
            {
                return new ApiResourceInfo(path, "POST", from.name);
            }
        });
    }

    public RpcEncodedSoapApiScopeHelper(String path, final String namespace,
                                        Collection<String> methods)
    {
        this(path, transform(methods, new Function<String, SoapScope>()
        {
            @Override
            public SoapScope apply(String from)
            {
                return new SoapScope(namespace, from);
            }
        }));
    }

    public boolean allow(HttpServletRequest request, String user)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo))
        {
            Document doc = readDocument(request);
            Element body = doc.getRootElement().element("Body");
            Element methodElement = (Element) body.elements().get(0);
            String name = methodElement.getName();
            String namespace = methodElement.getNamespaceURI();
            for (SoapScope scope : soapActions)
            {
                if (scope.match(namespace, name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    public static class SoapScope
    {
        private final String namespace;
        private final String name;

        public SoapScope(String namespace, String name)
        {
            this.namespace = namespace;
            this.name = name;
        }

        public boolean match(String namespace, String name)
        {
            return this.namespace.equals(namespace) && this.name.equals(name);
        }
    }
}
