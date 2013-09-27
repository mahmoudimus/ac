package com.atlassian.plugin.connect.plugin.module.context;

import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;

import java.util.Map;

/**
 * An interface for objects that deserialise request parameters into some application resource. The user must have
 * permission to view the resource.
 *
 */
public interface ParameterDeserializer<T>
{
    /**
     * De-serializes the given parameters into a resource that the user has access to. If the parameters don't contain
     * keys matching a resource or if the user has no view permission on that resource then the result is empty
     *
     * TODO: Would it be cleaner for implementations to only be passed the params that pertain to them rather than all the params?
     *
     * @param params the form params to check.
     * @return Optionally a deserialised resource.
     * @throws UnauthorisedException if the user does not have permission to view the resource
     * @throws ResourceNotFoundException if the a serialised resource is present in the params but does not represent an
     * existing resource. Note this may be thrown instead of UnauthorisedException if that is the policy of the application
     */
    Optional<T> deserialize(Map<String, Object> params, String username) throws UnauthorisedException, ResourceNotFoundException, MalformedRequestException;

}
