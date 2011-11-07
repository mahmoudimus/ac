package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 *
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
            String method = extractMethod(request);
            if (methods.contains(method))
            {
                return true;
            }
        }
        return false;
    }

    private String extractMethod(HttpServletRequest request)
    {
        SAXReader build = new SAXReader();
        InputStream in = null;
        try
        {
            in = request.getInputStream();
            Document doc = build.read(in);
            return doc.getRootElement().element("methodName").getTextTrim();
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (DocumentException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
