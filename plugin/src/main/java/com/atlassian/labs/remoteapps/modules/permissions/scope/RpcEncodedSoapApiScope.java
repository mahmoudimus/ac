package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.readDocument;
import static com.google.common.collect.Collections2.transform;

/**
 * An api scope for SOAP requests
 */
public class RpcEncodedSoapApiScope implements ApiScope
{
    private final String path;
    private final Collection<SoapScope> soapActions;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public RpcEncodedSoapApiScope(final String path, Collection<SoapScope> soapActions)
    {
        this.path = path;
        this.soapActions = soapActions;
        this.apiResourceInfo = Iterables.transform(soapActions, new Function<SoapScope, ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(SoapScope from)
            {
                return new ApiResourceInfo(path, HttpMethod.POST, from.name);
            }
        });
    }

    public RpcEncodedSoapApiScope(String path, final String namespace, Collection<String> methods)
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

    @Override
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

    @Override
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
