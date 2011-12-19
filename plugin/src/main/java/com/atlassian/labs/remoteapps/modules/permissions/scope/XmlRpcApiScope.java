package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.readDocument;

/**
 * An api scope for xml-rpc requests
 */
public class XmlRpcApiScope
{
    private final String path;
    private final Collection<String> methods;

    public XmlRpcApiScope(String path, Collection<String> methods)
    {
        this.path = path;
        this.methods = methods;
    }

    public boolean allow(HttpServletRequest request)
    {
        final String pathInfo = ServletUtils.extractPathInfo(request);
        if (path.equals(pathInfo))
        {
            Document doc = readDocument(request);
            String method = doc.getRootElement().element("methodName").getTextTrim();
            if (method == null)
            {
                return false;
            }
            else if (methods.contains(method))
            {
                return true;
            }
        }
        return false;
    }
}
