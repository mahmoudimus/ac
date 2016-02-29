package com.atlassian.plugin.connect.api.lifecycle;

/**
 * Exception that is thrown when enablement of a connect add-on fails,
 * e.g. when the associated user can not be enabled.
 */
public class ConnectAddonEnableException extends Exception {

    private final String addonKey;

    public ConnectAddonEnableException(String addonKey, String message, Throwable cause) {
        super(message, cause);
        this.addonKey = addonKey;
    }

    public ConnectAddonEnableException(String addonKey, String message) {
        super(message);
        this.addonKey = addonKey;
    }

    public String getAddonKey() {
        return addonKey;
    }
}
