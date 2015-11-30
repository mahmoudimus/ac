package com.atlassian.plugin.connect.api;

public class ConnectAddonEnableException extends Exception {

    public ConnectAddonEnableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectAddonEnableException(String message) {
        super(message);
    }
}
