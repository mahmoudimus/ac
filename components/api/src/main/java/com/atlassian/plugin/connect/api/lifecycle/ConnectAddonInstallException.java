package com.atlassian.plugin.connect.api.lifecycle;

import java.io.Serializable;

/**
 * Exception that is thrown when installation of a connect add-on fails
 *
 * @since 1.2
 */
public class ConnectAddonInstallException extends Exception {

    private final Serializable[] i18nArgs;
    private final String i18nKey;

    public ConnectAddonInstallException(String message)
    {
        super(message);

        i18nArgs = new Serializable[] {};
        i18nKey = null;
    }

    public ConnectAddonInstallException(String message, Throwable cause) {
        super(message, cause);

        i18nArgs = new Serializable[] {};
        i18nKey = null;
    }

    public ConnectAddonInstallException(String message, String i18nKey, Serializable... i18nArgs) {
        super(message);

        this.i18nArgs = i18nArgs;
        this.i18nKey = i18nKey;
    }

    public Serializable[] getI18nArgs() {
        return i18nArgs;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
