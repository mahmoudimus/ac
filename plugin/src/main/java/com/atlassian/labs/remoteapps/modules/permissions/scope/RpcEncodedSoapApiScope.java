package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.readDocument;
import static com.google.common.collect.Collections2.transform;

/**
 * An api scope for SOAP requests
 */
public class RpcEncodedSoapApiScope
{
    private final String path;
    private final Collection<SoapScope> soapActions;

    public RpcEncodedSoapApiScope(String path, Collection<SoapScope> soapActions)
    {
        this.path = path;
        this.soapActions = soapActions;
    }

    public boolean allow(HttpServletRequest request)
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
