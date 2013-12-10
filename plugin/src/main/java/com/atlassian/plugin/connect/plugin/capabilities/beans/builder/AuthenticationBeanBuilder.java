package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;

public class AuthenticationBeanBuilder extends BaseCapabilityBeanBuilder<AuthenticationBeanBuilder, AuthenticationBean>
{
    private AuthenticationType type;
    private String publicKey;
    
    public AuthenticationBeanBuilder()
    {
        this.type = AuthenticationType.JWT;
        this.publicKey = "";
    }

    public AuthenticationBeanBuilder(AuthenticationBean defaultBean)
    {
        this.publicKey = defaultBean.getPublicKey();
        this.type = defaultBean.getType();
    }

    public AuthenticationBeanBuilder withType(AuthenticationType type)
    {
        this.type = type;
        return this;
    }

    public AuthenticationBeanBuilder withPublicKey(String key)
    {
        this.publicKey = key;
        return this;
    }

    @Override
    public AuthenticationBean build()
    {
        return new AuthenticationBean(this);
    }
}
