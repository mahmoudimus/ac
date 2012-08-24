package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.ChainingFormBuilder;
import com.atlassian.labs.remoteapps.api.service.http.Request;

import java.util.List;

public class DefaultChainingFormBuilder implements ChainingFormBuilder
{
    private Request request;
    private DefaultFormBuilder delegate;

    public DefaultChainingFormBuilder(Request request)
    {
        this.request = request;
        delegate = new DefaultFormBuilder();
    }

    @Override
    public ChainingFormBuilder addParam(String name)
    {
        delegate.addParam(name);
        return this;
    }

    @Override
    public ChainingFormBuilder addParam(String name, String value)
    {
        delegate.addParam(name, value);
        return this;
    }

    @Override
    public ChainingFormBuilder setParam(String name, List<String> values)
    {
        delegate.setParam(name, values);
        return this;
    }

    @Override
    public String toEntity()
    {
        return delegate.toEntity();
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

    @Override
    public Request commit()
    {
        return request.setFormEntity(this);
    }
}
