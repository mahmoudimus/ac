package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.fugue.Option;

public class LifecycleCallbackException extends Exception
{
    private final Option<String> i18nKey;

    public LifecycleCallbackException(String message, Option<String> i18nKey)
    {
        super(message);
        this.i18nKey = i18nKey;
    }

    public Option<String> getI18nKey()
    {
        return i18nKey;
    }
}
