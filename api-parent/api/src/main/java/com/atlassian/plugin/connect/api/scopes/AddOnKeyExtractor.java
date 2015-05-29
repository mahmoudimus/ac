package com.atlassian.plugin.connect.api.scopes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public interface AddOnKeyExtractor
{
    /**
     * Request header set by /iframe/host/main.js, indicating that the current request is an XDM request. The value
     * is the key of the Connect add-on that made the XDM request.
     */
    String AP_REQUEST_HEADER = "AP-Client-Key";

    @Nullable
    String getAddOnKeyFromHttpRequest(@Nonnull HttpServletRequest req);

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
    boolean isAddOnRequest(@Nonnull HttpServletRequest request);

    /**
     * @param req the context {@link HttpServletRequest}
     * @return the unique add-on id, synonymous with OAuth client key and JWT issuer, or {@code null} if 2LO authentication failed or was not
     *         attempted
     */
    @Nullable
    String extractClientKey(@Nonnull HttpServletRequest req);

    /**
     * Set the id of a Connect add-on in the request attributes.
     *
     * @param req the context {@link HttpServletRequest}
     * @param clientKey the client key of the add-on
     */
    void setClientKey(@Nonnull HttpServletRequest req, @Nonnull String clientKey);
}
