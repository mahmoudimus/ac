package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean;

/**
 * @since 1.0
 */
public class OAuthBeanBuilder<T extends OAuthBeanBuilder, B extends OAuthBean> extends BaseCapabilityBeanBuilder<T,B>
{
    private String publicKey;
    private String callback;
    private String requestTokenUrl;
    private String accessTokenUrl;
    private String authorizeUrl;

    public OAuthBeanBuilder()
    {
    }

    public OAuthBeanBuilder(OAuthBean defaultBean)
    {
        this.publicKey = defaultBean.getPublicKey();
        this.callback = defaultBean.getCallback();
        this.requestTokenUrl = defaultBean.getRequestTokenUrl();
        this.accessTokenUrl = defaultBean.getAccessTokenUrl();
        this.authorizeUrl = defaultBean.getAuthorizeUrl();

    }

    public T withPublicKey(String key)
    {
        this.publicKey = key;
        return (T) this;
    }

    public T withCallback(String url)
    {
        this.callback = url;
        return (T) this;
    }

    public T withRequestTokenUrl(String url)
    {
        this.requestTokenUrl = url;
        return (T) this;
    }

    public T withAccessTokenUrl(String url)
    {
        this.accessTokenUrl = url;
        return (T) this;
    }

    public T withAuthorizeUrl(String url)
    {
        this.authorizeUrl = url;
        return (T) this;
    }

    public B build()
    {
        return (B) new OAuthBean(this);
    }
}
