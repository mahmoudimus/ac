package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.jwt.JwtConstants;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.api.scopes.AddOnKeyExtractor;
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
public class AddOnKeyExtractorImpl implements AddOnKeyExtractor
{
    /**
     * Set by a {@link javax.servlet.Filter}, possibly using {@link com.atlassian.plugin.connect.plugin.module.oauth.OAuth2LOAuthenticator} or {@link com.atlassian.jwt.plugin.sal.JwtAuthenticator},
     * indicating the Connect add-on that is the origin of the current request.
     */
    private static final String PLUGIN_KEY_ATTRIBUTE = JwtConstants.HttpRequests.ADD_ON_ID_ATTRIBUTE_NAME;

    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final ConsumerService consumerService;

    @Autowired
    public AddOnKeyExtractorImpl(final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService, ConsumerService consumerService)
    {
        this.jsonConnectAddOnIdentifierService = jsonConnectAddOnIdentifierService;
        this.consumerService = consumerService;
    }

    @Override
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

    @Override
    public boolean isAddOnRequest(@Nonnull HttpServletRequest request)
    {
        String addOnKey = extractClientKey(request);
        return (addOnKey != null && !consumerService.getConsumer().getKey().equals(addOnKey)) || (extractXdmRequestKey(request) != null);
    }

    @Override
    @Nullable
    public String extractClientKey(@Nonnull HttpServletRequest req)
    {
        return (String) req.getAttribute(PLUGIN_KEY_ATTRIBUTE);
    }


    @Override
    public void setClientKey(@Nonnull HttpServletRequest req, @Nonnull String clientKey)
    {
        req.setAttribute(PLUGIN_KEY_ATTRIBUTE, clientKey);
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
