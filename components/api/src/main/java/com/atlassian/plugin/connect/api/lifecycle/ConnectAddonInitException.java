package com.atlassian.plugin.connect.api.lifecycle;

public class ConnectAddonInitException extends RuntimeException
{
    public static final String USER_PROVISIONING_ERROR = "connect.install.error.user.provisioning";
    public static final String ADDON_ADMINS_MISSING_PERMISSION = "connect.install.error.addon.admin.permission";

    private String i18nkey;

    public ConnectAddonInitException(Exception cause)
    {
        this(cause, USER_PROVISIONING_ERROR);
    }

    public ConnectAddonInitException(Exception cause, String i18nKey)
    {
        super(cause);
        this.i18nkey = i18nKey;
    }

    public ConnectAddonInitException(String message)
    {
        this(message, USER_PROVISIONING_ERROR);
    }

    public ConnectAddonInitException(String message, String i18nKey)
    {
        super(message);
        this.i18nkey = i18nKey;
    }

    public String getI18nKey()
    {
        return this.i18nkey;
    }
}
