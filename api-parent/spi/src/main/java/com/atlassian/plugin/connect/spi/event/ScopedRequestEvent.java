package com.atlassian.plugin.connect.spi.event;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.analytics.api.annotations.PrivacyPolicySafe;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.spi.permission.scope.JsonRpcApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.RpcEncodedSoapApiScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.XmlRpcApiScopeHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@PrivacyPolicySafe
public abstract class ScopedRequestEvent
{
    private static String REST_URI_PATH_PREFIX = "rest/";

    private static final List<String> XMLRPC_PATHS = ImmutableList.of("/rpc/xmlrpc");

    private static final List<String> JSON_RPC_PATHS = ImmutableList.of("/rpc/json-rpc/confluenceservice-v1",
                                                                        "/rpc/json-rpc/confluenceservice-v2",
                                                                        "rpc/json-rpc/jirasoapservice-v2");
    private static final List<String> SOAP_PATHS = ImmutableList.of("/rpc/soap/jirasoapservice-v2",
                                                                    "/soap/axis/confluenceservice-v2",
                                                                    "/soap/axis/confluenceservice-v1");

    @PrivacyPolicySafe private final String httpMethod;

    @PrivacyPolicySafe private final String httpRequestUri;

    public ScopedRequestEvent(HttpServletRequest rq)
    {
        super();
        this.httpMethod = rq.getMethod();
        this.httpRequestUri = toAnalyticsSafePath(rq);
    }

    private static Predicate<String> endsWith(final String it)
    {
        return new Predicate<String>(){

            @Override
            public boolean apply(@Nullable String suffix)
            {
                return it.endsWith(suffix);
            }};
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
            Option<Pair<String,String>> maybeMethod = RpcEncodedSoapApiScopeHelper.getMethod(rq);
            if(maybeMethod.isEmpty())
            {
                return path;
            }
            // We're ignoring the namespace
            String method = maybeMethod.get().right();
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
