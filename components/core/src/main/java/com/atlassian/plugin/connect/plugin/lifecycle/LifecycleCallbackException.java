package com.atlassian.plugin.connect.plugin.lifecycle;

import java.io.Serializable;

import com.google.common.collect.ImmutableList;

public class LifecycleCallbackException extends Exception
{
    private final String i18nKey;

    private final Serializable[] params;

    public LifecycleCallbackException(String message, String i18nKey)
    {
        this(message, i18nKey, ImmutableList.of());
    }

    public LifecycleCallbackException(String message, String i18nKey, Serializable... params)
    {
        super(message);
        this.i18nKey = i18nKey;
        this.params = params;
    }

    public String getI18nKey()
    {
        return i18nKey;
    }

    public Serializable[] getParams()
    {
        return params;
    }
}
