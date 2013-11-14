package com.atlassian.plugin.connect.plugin.module.permission;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public interface RequestAddOnKeyLabeler
{
    /**
     * @param request the context {@link HttpServletRequest}
     * @return the unique identifier of the remote app, such as an OAuth1 consumer key or JWT issuer, or {@code null} if authentication failed or was not attempted
     */
    @Nullable
    String getAddOnKey(HttpServletRequest request);

    /**
     * Set the unique identifier of the remote app for later retrieval by {@link #getAddOnKey(HttpServletRequest)}.
     * @param request the context {@link HttpServletRequest}
     * @param addOnKey the unique identifier of the remote app, such as an OAuth1 consumer key or JWT issuer
     */
    void setAddOnKey(HttpServletRequest request, String addOnKey);
}
