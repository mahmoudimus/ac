package com.atlassian.plugin.connect.plugin.lifecycle.event;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConnectAddonLifecycleFailedEvent extends ConnectAddonLifecycleEvent
{
    private static final int MAX_MESSAGE_LENGTH = 100;

    /**
     * The HTTP status code of the failed HTTP lifecycle request
     */
    private final Integer statusCode;

    /**
     * The reason why the lifecycle event failed
     */
    private final String message;

    /**
     * Categorisation of why the lifecycle event failed (to facilitate analysis).
     */
    private final Category category;

    public enum Category
    {
        /**
         * The problem appears to lie with the add-on (e.g. it did not respond nicely to a callback).
         */
        ADD_ON,

        /**
         * The problem appears to lie with Connect (e.g. an exception that we couldn't explicitly categorise).
         */
        CONNECT;
    }

    public ConnectAddonLifecycleFailedEvent(String pluginKey, String message, Category category)
    {
        this(pluginKey, null, message, category);
    }

    public ConnectAddonLifecycleFailedEvent(String pluginKey, Integer statusCode, String message, Category category)
    {
        super(pluginKey);
        this.statusCode = statusCode;
        this.message = StringUtils.substring(message, 0, MAX_MESSAGE_LENGTH);
        this.category = checkNotNull(category);
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public String getMessage()
    {
        return message;
    }

    public Category getCategory()
    {
        return category;
    }
}
