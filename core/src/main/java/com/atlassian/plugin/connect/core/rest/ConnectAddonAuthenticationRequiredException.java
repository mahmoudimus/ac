package com.atlassian.plugin.connect.core.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Exception thrown when a client tries to access a resource that requires authentication by an add-on.
 *
 * @see com.atlassian.plugins.rest.common.security.AuthenticationRequiredException
 */
@XmlRootElement
public class ConnectAddonAuthenticationRequiredException extends SecurityException
{
    public ConnectAddonAuthenticationRequiredException()
    {
        super("The requested resource requires authentication as an add-on");
    }

    public ConnectAddonAuthenticationRequiredException(String message)
    {
        super(message);
    }

    public ConnectAddonAuthenticationRequiredException(String message, Throwable throwable)
    {
        super(message, throwable);
    }

    public ConnectAddonAuthenticationRequiredException(Throwable throwable)
    {
        super(throwable);
    }

    @Override
    @XmlElement
    public String getMessage()
    {
        return super.getMessage();
    }
}
