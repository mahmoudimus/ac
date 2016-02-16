package com.atlassian.plugin.connect.plugin.lifecycle.event;

import com.atlassian.plugin.connect.plugin.lifecycle.LifecycleCallbackException;

import java.io.Serializable;

public class LifecycleCallbackBadResponseException extends LifecycleCallbackException
{
    public LifecycleCallbackBadResponseException(String message, String i18nKey)
    {
        super(message, i18nKey);
    }

    public LifecycleCallbackBadResponseException(String message, String i18nKey, Serializable... params)
    {
        super(message, i18nKey, params);
    }
}
