package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;

public class AuthenticationBeanBuilder extends BaseCapabilityBeanBuilder<AuthenticationBeanBuilder,AuthenticationBean>
{
    private AuthenticationType type;
    private String sharedKey;
    
    public AuthenticationBeanBuilder()
    {
        this.type = AuthenticationType.JWT;
        this.sharedKey = "";
    }

    public AuthenticationBeanBuilder(AuthenticationBean defaultBean)
    {
        this.sharedKey = defaultBean.getSharedKey();
        this.type = defaultBean.getType();
    }

    public AuthenticationBeanBuilder withType(AuthenticationType type)
    {
        this.type = type;
        return this;
    }

    public AuthenticationBeanBuilder withSharedKey(String key)
    {
        this.sharedKey = key;
        return this;
    }

    @Override
    public AuthenticationBean build()
    {
        return new AuthenticationBean(this);
    }
}
