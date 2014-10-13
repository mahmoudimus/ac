package com.atlassian.plugin.connect.plugin.usermanagement;

/**
 * todo: write javadoc
 */
public class ConnectAddOnUserInitException extends RuntimeException
{
    private String i18nkey;

    public ConnectAddOnUserInitException(Exception cause)
    {
        this(cause, ConnectAddOnUserProvisioningService.USER_PROVISIONING_ERROR);
    }

    public ConnectAddOnUserInitException(Exception cause, String i18nKey)
    {
        super(cause);
        this.i18nkey = i18nKey;
    }

    public ConnectAddOnUserInitException(String message)
    {
        this(message, ConnectAddOnUserProvisioningService.USER_PROVISIONING_ERROR);
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
