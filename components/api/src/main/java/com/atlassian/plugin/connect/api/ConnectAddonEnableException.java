package com.atlassian.plugin.connect.api;

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
    
    public String getAddonKey()
    {
        return addonKey;
    }
}
