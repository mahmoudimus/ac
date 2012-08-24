package com.atlassian.labs.remoteapps.api.service.http;

import java.util.List;

/**
 * A specialization of {@link FormBuilder} that adds the ability to be chained with a request
 * builder.
 */
public interface ChainingFormBuilder extends FormBuilder
{
    @Override
    public ChainingFormBuilder addParam(String name);

    @Override
    public ChainingFormBuilder addParam(String name, String value);

    @Override
    public ChainingFormBuilder setParam(String name, List<String> values);

    /**
     * Returns the associated request for which this builder is building a URL-encoded form
     * entity string, after setting its built entity and appropriate content-type as properties
     * of that request.
     *
     * @return The associated request
     */
    public Request commit();
}
