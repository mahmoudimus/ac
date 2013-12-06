package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.AuthenticationBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    // don't call super because BaseCapabilityBean has no data
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof AuthenticationBean))
        {
            return false;
        }

        AuthenticationBean other = (AuthenticationBean) otherObj;

        return new EqualsBuilder()
                .append(type, other.type)
                .append(sharedKey, other.sharedKey)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(59, 5)
                .append(type)
                .append(sharedKey)
                .build();
    }
}
