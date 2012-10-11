package com.atlassian.plugin.remotable.spi.permission.scope;

import com.atlassian.plugin.remotable.spi.util.ServletUtils;
import com.google.common.base.Function;
import org.dom4j.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.readDocument;
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
            public ApiResourceInfo apply(String from)
            {
                return new ApiResourceInfo(path, "POST", from);
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
