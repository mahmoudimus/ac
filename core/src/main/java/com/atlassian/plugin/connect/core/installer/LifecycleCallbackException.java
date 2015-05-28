package com.atlassian.plugin.connect.core.installer;

import java.io.Serializable;

public class LifecycleCallbackException extends Exception
{
    private final String i18nKey;

    private final Serializable[] params;

    public LifecycleCallbackException(String message)
    {
        this(message, null, null);
    }

    public LifecycleCallbackException(String message, String i18nKey)
    {
        this(message, i18nKey, null);
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
