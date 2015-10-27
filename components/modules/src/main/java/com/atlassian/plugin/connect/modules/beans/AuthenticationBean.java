package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.AuthenticationBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Defines the authentication type to use when signing requests between the host application and the connect add on.
 * The authentication type can be JWT or none. If the type is not supplied it will default to JWT.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#AUTHENTICATION_EXAMPLE}
 * @schemaTitle Authentication
 */
public class AuthenticationBean extends BaseModuleBean
{
    /**
     * The type of authentication to use.
     */
    @CommonSchemaAttributes(defaultValue = "jwt")
    private AuthenticationType type;

    /**
     * The public key used for asymmetric key encryption. Ignored when using JWT with a shared secret.
     */
    private String publicKey;

    public AuthenticationBean()
    {
        this.type = AuthenticationType.JWT;
        this.publicKey = "";
    }

    public AuthenticationBean(AuthenticationBeanBuilder builder)
    {
        super(builder);
        if (null == publicKey)
        {
            this.publicKey = "";
        }
        if (null == type)
        {
            this.type = AuthenticationType.JWT;
        }
    }

    public AuthenticationType getType()
    {
        return type;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public static AuthenticationBeanBuilder newAuthenticationBean()
    {
        return new AuthenticationBeanBuilder();
    }

    public static AuthenticationBeanBuilder newAuthenticationBean(AuthenticationBean defaultBean)
    {
        return new AuthenticationBeanBuilder(defaultBean);
    }

    public static AuthenticationBean none()
    {
        return newAuthenticationBean().withType(AuthenticationType.NONE).build();
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
                .append(publicKey, other.publicKey)
                .isEquals();
    }

    // don't call super because BaseCapabilityBean has no data
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(59, 5)
                .append(type)
                .append(publicKey)
                .build();
    }
}
