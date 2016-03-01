package com.atlassian.plugin.connect.modules.beans;

import java.io.Serializable;

/**
 * An exception thrown when syntactic or semantic validation of a descriptor module fails.
 */
public class ConnectModuleValidationException extends Exception {

    private final ShallowConnectAddonBean addon;
    private final ConnectModuleMeta moduleMeta;
    private final String i18nKey;
    private final Serializable[] i18nParameters;

    public ConnectModuleValidationException(ShallowConnectAddonBean addon, ConnectModuleMeta moduleMeta, String message,
                                            String i18nKey, Serializable... i18nParameters) {
        super(message);
        this.addon = addon;
        this.moduleMeta = moduleMeta;
        this.i18nKey = i18nKey;
        this.i18nParameters = i18nParameters;
    }

    public ShallowConnectAddonBean getAddon() {
        return addon;
    }

    public ConnectModuleMeta getModuleMeta() {
        return moduleMeta;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public Serializable[] getI18nParameters() {
        return i18nParameters;
    }
}
