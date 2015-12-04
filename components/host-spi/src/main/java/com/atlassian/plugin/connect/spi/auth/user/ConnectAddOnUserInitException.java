package com.atlassian.plugin.connect.spi.auth.user;

public class ConnectAddOnUserInitException extends RuntimeException
{
    public static final String USER_PROVISIONING_ERROR = "connect.install.error.user.provisioning";
    public static final String ADDON_ADMINS_MISSING_PERMISSION = "connect.install.error.addon.admin.permission";

    private String i18nkey;

    public ConnectAddOnUserInitException(Exception cause)
    {
        this(cause, USER_PROVISIONING_ERROR);
    }

    public ConnectAddOnUserInitException(Exception cause, String i18nKey)
    {
        super(cause);
        this.i18nkey = i18nKey;
    }

    public ConnectAddOnUserInitException(String message)
    {
        this(message, USER_PROVISIONING_ERROR);
    }

    public ConnectAddOnUserInitException(String message, String i18nKey)
    {
        super(message);
        this.i18nkey = i18nKey;
    }

    public String getI18nKey()
    {
        return this.i18nkey;
    }
}
