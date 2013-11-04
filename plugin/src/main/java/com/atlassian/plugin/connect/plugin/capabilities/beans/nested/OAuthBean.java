package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.OAuthBeanBuilder;

import com.google.common.base.Objects;

/**
 * @since 1.0
 */
public class OAuthBean extends BaseCapabilityBean
{
    /**
     * The PEM-encoded public key to be used to sign all requests from this add-on to the target Atlassian
     * application. The key value must start with "-----BEGIN PUBLIC KEY-----".
     */
    private String publicKey;

    /**
     * The add-on host's callback URL to use in the OAuth dance.
     */
    private String callback;

    private String requestTokenUrl;

    private String accessTokenUrl;

    private String authorizeUrl;

    public OAuthBean()
    {
        this.publicKey = "";
        this.callback = "";
        this.requestTokenUrl = "";
        this.accessTokenUrl = "";
        this.authorizeUrl = "";
    }

    public OAuthBean(OAuthBeanBuilder builder)
    {
        super(builder);

        if(null == publicKey)
        {
            this.publicKey = "";
        }

        if(null == callback)
        {
            this.callback = "";
        }

        if(null == requestTokenUrl)
        {
            this.requestTokenUrl = "";
        }

        if(null == accessTokenUrl)
        {
            this.accessTokenUrl = "";
        }

        if(null == authorizeUrl)
        {
            this.authorizeUrl = "";
        }
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public String getCallback()
    {
        return callback;
    }

    public String getRequestTokenUrl()
    {
        return requestTokenUrl;
    }

    public String getAccessTokenUrl()
    {
        return accessTokenUrl;
    }

    public String getAuthorizeUrl()
    {
        return authorizeUrl;
    }

    public static OAuthBeanBuilder newOAuthBean()
    {
        return new OAuthBeanBuilder();
    }

    public static OAuthBeanBuilder newOAuthBean(OAuthBean defaultBean)
    {
        return new OAuthBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(publicKey, callback, requestTokenUrl, accessTokenUrl, authorizeUrl);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof OAuthBean))
        {
            return false;
        }
        else
        {
            final OAuthBean that = (OAuthBean) obj;
            return Objects.equal(publicKey, that.publicKey) &&
                    Objects.equal(callback, that.callback) &&
                    Objects.equal(requestTokenUrl, that.requestTokenUrl) &&
                    Objects.equal(accessTokenUrl, that.accessTokenUrl) &&
                    Objects.equal(authorizeUrl, that.authorizeUrl);
        }
    }
}

