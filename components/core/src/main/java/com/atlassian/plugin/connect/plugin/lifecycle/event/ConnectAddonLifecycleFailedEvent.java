package com.atlassian.plugin.connect.plugin.lifecycle.event;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConnectAddonLifecycleFailedEvent extends ConnectAddonLifecycleEvent {
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

    public enum Category {
        /**
         * The problem appears to lie with the add-on (e.g. it did not respond nicely to a callback).
         */
        ADDON,

        /**
         * The problem appears to lie with Connect (e.g. an exception that we couldn't explicitly categorise).
         */
        CONNECT;

        /**
         * Emit the {@link #name()} in lower case for easier readability in analytics logs.
         *
         * @return {@link #name()} in lower case
         */
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public ConnectAddonLifecycleFailedEvent(String addonKey, String message, Category category) {
        this(addonKey, null, message, category);
    }

    public ConnectAddonLifecycleFailedEvent(String addonKey, Integer statusCode, String message, Category category) {
        super(addonKey);
        this.statusCode = statusCode;
        this.message = StringUtils.substring(message, 0, MAX_MESSAGE_LENGTH);
        this.category = checkNotNull(category);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Category getCategory() {
        return category;
    }
}
