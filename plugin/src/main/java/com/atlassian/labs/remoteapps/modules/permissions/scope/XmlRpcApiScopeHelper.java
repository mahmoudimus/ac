package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.util.ServletUtils;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.readDocument;
import static com.google.common.collect.Iterables.transform;

/**
 * An api scope for xml-rpc requests
 */
public class XmlRpcApiScopeHelper
{
    private final String path;
    private final Collection<String> methods;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    public XmlRpcApiScopeHelper(final String path, Collection<String> methods)
    {
        this.path = path;
        this.methods = methods;
        this.apiResourceInfo = transform(methods, new Function<String,ApiResourceInfo>()
        {
            @Override
            public ApiResourceInfo apply(@Nullable String from)
            {
                return new ApiResourceInfo(path, HttpMethod.POST, from);
            }
        });
    }

    public boolean allow(HttpServletRequest request, String user)
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

    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }
}
