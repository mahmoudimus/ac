package com.atlassian.plugin.connect.core.util;

import java.io.Serializable;

import com.atlassian.sal.api.message.Message;

/**
 *
 */
public class DefaultMessage implements Message
{
    private final String key;

    public DefaultMessage(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public Serializable[] getArguments()
    {
        return new Serializable[0];
    }
}
