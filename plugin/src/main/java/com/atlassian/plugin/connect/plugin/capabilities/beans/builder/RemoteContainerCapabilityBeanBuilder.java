package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean;

/**
 * @since version
 */
public class RemoteContainerCapabilityBeanBuilder<T extends RemoteContainerCapabilityBeanBuilder, B extends RemoteContainerCapabilityBean> extends BaseCapabilityBeanBuilder<T,B>
{
    private String displayUrl;
    private OAuthBean oauth;

    public RemoteContainerCapabilityBeanBuilder()
    {
    }

    public RemoteContainerCapabilityBeanBuilder(RemoteContainerCapabilityBean defaultBean)
    {
        this.displayUrl = defaultBean.getDisplayUrl();
        this.oauth = defaultBean.getOauth();
    }

    public T withDisplayUrl(String url)
    {
        this.displayUrl = url;
        return (T) this;
    }

    public T withOAuth(OAuthBean oauth)
    {
        this.oauth = oauth;
        return (T) this;
    }

    public B build()
    {
        return (B) new RemoteContainerCapabilityBean(this);
    }
}
