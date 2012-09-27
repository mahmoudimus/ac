package com.atlassian.labs.remoteapps.plugin.util.contextparameter;

import com.atlassian.sal.api.user.UserManager;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Creates a context-aware instance of {@link RequestContextParameters} for use in an iframe URL
 * or HTTP content retrieval call.
 */
public class RequestContextParameterFactory
{
    private final Set<String> queryParameters;
    private final Set<String> headerParameters;
    private final boolean legacyMode;

    public RequestContextParameterFactory(Set<String> queryParameters, Set<String> headerParameters)
    {
        this.queryParameters = queryParameters;
        this.headerParameters = headerParameters;
        this.legacyMode = false;
    }

    public RequestContextParameterFactory()
    {
        this.legacyMode = true;
        this.queryParameters = Collections.emptySet();
        this.headerParameters = Collections.emptySet();
    }

    /**
     * Creates a context-aware instance of {@link RequestContextParameters}.  To create the instance,
     * the passed entity-related context parameters are combined with global context parameters such
     * as user_id
     */
    public RequestContextParameters create(String userId, Map<String, String> entityContextParameters)
    {
        Map<String, String> allContextParameters = newHashMap();
        allContextParameters.put("user_id", userId != null ? userId : "");
        allContextParameters.putAll(entityContextParameters);

        return new RequestContextParameters(allContextParameters, queryParameters, headerParameters,
                legacyMode);
    }



}
