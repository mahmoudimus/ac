package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.AuthenticationBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseCapabilityBeanBuilder;

public class AuthenticationBean extends BaseCapabilityBean
{
    private AuthenticationType type;
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
