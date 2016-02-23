package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.connect.plugin.lifecycle.event.LifecycleCallbackBadResponseException;

public class LifecycleCallbackHttpCodeException extends LifecycleCallbackBadResponseException {
    private final int httpCode;

    public LifecycleCallbackHttpCodeException(String message, String i18nKey, final int httpCode) {
        super(message, i18nKey);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
