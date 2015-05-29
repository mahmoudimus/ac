package com.atlassian.plugin.connect.api.util;

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

    public RequestContextParameterFactory(Set<String> queryParameters, Set<String> headerParameters)
    {
        this.queryParameters = queryParameters;
        this.headerParameters = headerParameters;
    }

    /**
     * Creates a context-aware instance of {@link RequestContextParameters}.  To create the instance,
     * the passed entity-related context parameters are combined with global context parameters such
     * as user_id
     *
     * @param userId the user ID to include in the context
     * @param userKey the user key to include in the context
     * @param entityContextParameters parameters related to the entity of the current context
     * @return the request context parameters
     */
    public RequestContextParameters create(String userId, String userKey, Map<String, String> entityContextParameters)
    {
        Map<String, String> allContextParameters = newHashMap();
        allContextParameters.put("user_id", userId != null ? userId : "");
        allContextParameters.put("user_key", userKey != null ? userKey : "");
        allContextParameters.putAll(entityContextParameters);

        return new RequestContextParameters(allContextParameters, queryParameters, headerParameters);
    }



}
