package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.AuthenticationBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;

/**
 * Defines the authentication type to use when signing requests between the host application and the connect add on.
 * The authentication type can be eithe jwt or oauth. If the type is not supplied it will default to jwt.
 */
public class AuthenticationBean extends BaseCapabilityBean
{
    /**
     * The type of authentication to use. Defaults to jwt.
     */
    private AuthenticationType type;

    /**
     * Either the JWT shared secret ot the OAUTH publickKey depending on authentication type.
     */
    private String sharedKey;

    public AuthenticationBean()
    {
        this.type = AuthenticationType.JWT;
        this.sharedKey = "";
    }

    public AuthenticationBean(AuthenticationBeanBuilder builder)
    {
        super(builder);
        if(null == sharedKey)
        {
            this.sharedKey = "";
        }
        if(null == type)
        {
            this.type = AuthenticationType.JWT;
        }
    }

    public AuthenticationType getType()
    {
        return type;
    }

    public String getSharedKey()
    {
        return sharedKey;
    }

    public static AuthenticationBeanBuilder newAuthenticationBean()
    {
        return new AuthenticationBeanBuilder();
    }

    public static AuthenticationBeanBuilder newAuthenticationBean(AuthenticationBean defaultBean)
    {
        return new AuthenticationBeanBuilder(defaultBean);
    }
}
