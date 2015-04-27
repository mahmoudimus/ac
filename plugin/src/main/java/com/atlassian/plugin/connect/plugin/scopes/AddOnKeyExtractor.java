package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Class allowing for extracting of plugin key from http requests.
 */
@Component
public class AddOnKeyExtractor
{
    /**
     * Set by a {@link javax.servlet.Filter}, possibly using {@link com.atlassian.plugin.connect.plugin.module.oauth.OAuth2LOAuthenticator} or {@link com.atlassian.jwt.plugin.sal.JwtAuthenticator},
     * indicating the Connect add-on that is the origin of the current request.
     */
    private static final String PLUGIN_KEY = JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME;

    /**
     * Request header set by /iframe/host/main.js, indicating that the current request is an XDM request. The value
     * is the key of the Connect add-on that made the XDM request.
     */
    public static final String AP_REQUEST_HEADER = "AP-Client-Key";

    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final String ourConsumerKey;

    @Autowired
    public AddOnKeyExtractor(final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, ConsumerService consumerService)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.ourConsumerKey = consumerService.getConsumer().getKey();
    }

    @Nullable
    public String getAddOnKeyFromHttpRequest(@Nonnull HttpServletRequest req)
    {
        String addOnKey = extractClientKey(req);
        if (addOnKey != null)
        {
            return addOnKey;
        }
        addOnKey = extractXdmRequestKey(req);
        if (addOnKey != null && jsonConnectAddOnIdentifierService.isConnectAddOn(addOnKey))
        {
            return addOnKey;
        }
        return null;
    }

    /**
     * Checks to see if the request have been made from an add-on. We consider the request to be issued by and add-on
     * in the following cases.
     * 1. When the request is coming from an add-on server, is it attributed with Plugin-Key and has to be equal to the client key.
     * 2. When the request is coming from a browser, it comes with AP-Client-Key header.
     * This function does not check if the add-on exists, only if the the request could have been made by an add-on.
     *
     * @param request the http request where the key is looked for
     * @return if the request has been made by an add-on.
     */
    public boolean isAddOnRequest(@Nonnull HttpServletRequest request)
    {
        String addOnKey = extractClientKey(request);
        return (addOnKey != null && !ourConsumerKey.equals(addOnKey)) || (extractXdmRequestKey(request) != null);
    }

    /**
     * @param req the context {@link HttpServletRequest}
     * @return the unique add-on id, synonymous with OAuth client key and JWT issuer, or {@code null} if 2LO authentication failed or was not
     *         attempted
     */
    @Nullable
    public static String extractClientKey(@Nonnull HttpServletRequest req)
    {
        return (String) req.getAttribute(PLUGIN_KEY);
    }

    /**
     * Set the id of a Connect add-on in the request attributes.
     *
     * @param req the context {@link HttpServletRequest}
     * @param clientKey the client key of the add-on
     */
    public static void setClientKey(@Nonnull HttpServletRequest req, @Nonnull String clientKey)
    {
        req.setAttribute(PLUGIN_KEY, clientKey);
    }

    /**
     * @param req the context {@link javax.servlet.http.HttpServletRequest}
     * @return a {@link #AP_REQUEST_HEADER header} set by the XDM host library, indicating the current request is a host-mediated XHR sent on
     *         behalf of an add-on running in a sandboxed iframe; see AP.request(...) in the host-side AP js
     */
    @Nullable
    private static String extractXdmRequestKey(HttpServletRequest req)
    {
        return req.getHeader(AP_REQUEST_HEADER);
    }

}
