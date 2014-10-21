package com.atlassian.plugin.connect.spi.event;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.spi.scope.JsonRpcApiScopeHelper;
import com.atlassian.plugin.connect.spi.scope.RpcEncodedSoapApiScopeHelper;
import com.atlassian.plugin.connect.spi.scope.XmlRpcApiScopeHelper;
import com.atlassian.plugin.connect.spi.util.ServletUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public abstract class ScopedRequestEvent
{
    private static String REST_URI_PATH_PREFIX = "rest/";

    private static final List<String> XMLRPC_PATHS = ImmutableList.of("/rpc/xmlrpc");

    private static final List<String> JSON_RPC_PATHS = ImmutableList.of("/rpc/json-rpc/confluenceservice-v1",
                                                                        "/rpc/json-rpc/confluenceservice-v2",
                                                                        "/rpc/json-rpc/jirasoapservice-v2");

    private static final List<String> SOAP_PATHS = ImmutableList.of("/rpc/soap/jirasoapservice-v2",
                                                                    "/soap/axis/confluenceservice-v2",
                                                                    "/soap/axis/confluenceservice-v1");

    private final String httpMethod;

    private final String httpRequestUri;

    public ScopedRequestEvent(HttpServletRequest rq)
    {
        super();
        this.httpMethod = rq.getMethod();
        this.httpRequestUri = toAnalyticsSafePath(rq);
    }

    private static Predicate<String> endsWith(final String it)
    {
        return new Predicate<String>()
        {

            @Override
            public boolean apply(@Nullable String suffix)
            {
                return it.endsWith(suffix);
            }
        };
    }
 
    private static Predicate<String> startsWith(final String it)
    {
        return new Predicate<String>()
            {

                @Override
                public boolean apply(@Nullable String prefix)
                {
                    return it.startsWith(prefix);
                }
            };
    }

    private static boolean isXmlRpcUri(String uri)
    {
        return Iterables.any(XMLRPC_PATHS, endsWith(uri));
    }

    private static boolean isJsonRpcUri(String uri)
    {
        return Iterables.any(JSON_RPC_PATHS, endsWith(uri));
    }

    private static boolean isSoapUri(String uri)
    {
        return Iterables.any(SOAP_PATHS, endsWith(uri));
    }

    private static boolean isJsonRpcLightUri(HttpServletRequest rq)
    {
        String pathInfo = ServletUtils.extractPathInfo(rq);
        return Iterables.any(JSON_RPC_PATHS, startsWith(pathInfo));
    }

    /**
     * Trim potentially sensitive values from REST calls, append method name for SOAP/RPC.
     * 
     * @param rq
     * @return a path that is safe to use for analytics
     */

    private static String toAnalyticsSafePath(HttpServletRequest rq)
    {

        String path = StringUtils.removeEnd(ServletUtils.extractPathInfo(rq), "/");

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
            Option<Pair<String, String>> maybeMethod = RpcEncodedSoapApiScopeHelper.getMethod(rq);
            if (maybeMethod.isEmpty())
            {
                return path;
            }
            // We're ignoring the namespace
            String method = maybeMethod.get().right();
            return path + "/" + method;
        }
        else if(isJsonRpcLightUri(rq))
        {
            return path;
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
        StringBuilder uriBuilder = new StringBuilder();
        int count = 0;
        for (String elem : pathElems)
        {
            if (!isVersionNumber(elem))
            {
                ++count;
            }
            uriBuilder.append(elem.split("\\?")[0]);
            uriBuilder.append("/");

            if (count > 1)
            {
                break;
            }
        }

        return StringUtils.removeEnd(uriBuilder.toString(), "/");
    }

    private static boolean isVersionNumber(String pathElem)
    {
        if (pathElem.equals("latest"))
        {
            return true;
        }
        else
        {
            try
            {
                Double.valueOf(pathElem);
                return true;
            }
            catch (NumberFormatException nfe)
            {
                return false;
            }
        }
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
