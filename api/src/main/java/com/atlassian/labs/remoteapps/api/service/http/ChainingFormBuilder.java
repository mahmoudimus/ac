package com.atlassian.labs.remoteapps.api.service.http;

import java.util.List;

/**
 *
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
     *
     *
     * @return
     */
    public Request commit();
}
