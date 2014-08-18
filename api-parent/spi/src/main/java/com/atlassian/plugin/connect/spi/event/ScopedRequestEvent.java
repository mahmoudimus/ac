package com.atlassian.plugin.connect.spi.event;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;
import com.atlassian.plugin.connect.spi.permission.scope.JsonRpcApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.RpcEncodedSoapApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.XmlRpcApiScopeHelper;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{

    private static final String XMLRPC_PATH = "/rpc/xmlrpc";

    private static String REST_URI_PATH_PREFIX = "rest/";

    @PrivacyPolicySafe private final String httpMethod;

    @PrivacyPolicySafe private final String httpRequestUri;

    public ScopedRequestEvent(HttpServletRequest rq)
    {
        super();
        this.httpMethod = rq.getMethod();
        this.httpRequestUri = toAnalyticsSafePath(rq);
    }

    private static boolean isXmlRpcUri(String uri)
    {
        return uri.endsWith(XMLRPC_PATH);
    }

    private static boolean isJsonRpcUri(String uri)
    {
        return uri.endsWith("/rpc/json-rpc/confluenceservice-v2") || uri.endsWith("rpc/json-rpc/jirasoapservice-v2");
    }

    private static boolean isSoapUri(String uri)
    {
        return uri.endsWith("/rpc/soap/jirasoapservice-v2") || uri.endsWith("/soap/axis/confluenceservice-v2");
    }

    /**
     * Trim potentially sensitive values from REST calls, append method name for SOAP/RPC.
     * 
     * @param rq
     * @return a path that is safe to use for analytics
     */

    private static String toAnalyticsSafePath(HttpServletRequest rq)
    {
        String path = URI.create(rq.getRequestURI()).getPath();

        if (isXmlRpcUri(path))
        {
            String method = XmlRpcApiScopeHelper.extractMethod(rq);
            return path + "/" + method;
        }
        else if (isJsonRpcUri(path))
        {
            String method = JsonRpcApiScopeHelper.extractMethod(rq);
            return path + "/" + method;
        }
        else if (isSoapUri(path))
        {
            // We're ignoring the namespace
            String method = RpcEncodedSoapApiScopeHelper.getMethod(rq).right();
            return path + "/" + method;
        }
        else
        {
            return trimRestPath(path);
        }
    }

    /**
     * Trim REST API urls to remove sensitive information and to avoid having too many discrete values.
     * 
     * We take the first two elements of the path after '/rest' if the path does not include a version number
     * (Confluence), and first three elements if it includes a version number (JIRA).
     * 
     * 
     * @param uri
     * @return trimmed URI
     */
    private static String trimRestPath(String uri)
    {
        String[] pathElems = StringUtils.substringAfter(uri, REST_URI_PATH_PREFIX).split("/");
        StringBuffer uriBuffer = new StringBuffer();
        int count = 0;
        for (String elem : pathElems)
        {
            try
            {
                Integer.valueOf(elem);
                uriBuffer.append(elem);
                uriBuffer.append("/");
            }
            catch (NumberFormatException e)
            {
                ++count;
                uriBuffer.append(elem.split("\\?")[0]);
                uriBuffer.append("/");
            }
            if (count > 1)
            {
                break;
            }
        }

        return StringUtils.removeEnd(uriBuffer.toString(), "/");
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getHttpRequestUri()
    {
        return httpRequestUri;
    }
}
