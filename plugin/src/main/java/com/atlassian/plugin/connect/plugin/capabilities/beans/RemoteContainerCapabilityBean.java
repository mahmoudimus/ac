package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.RemoteContainerCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.RemoteContainerModuleProvider;

import com.google.common.base.Objects;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;

@CapabilitySet(key = "connect-container", moduleProvider = RemoteContainerModuleProvider.class)
public class RemoteContainerCapabilityBean extends BaseCapabilityBean
{
    private String displayUrl;
    private OAuthBean oauth;

    public RemoteContainerCapabilityBean()
    {
        this.displayUrl = "";
        this.oauth = newOAuthBean().build();
    }

    public RemoteContainerCapabilityBean(RemoteContainerCapabilityBeanBuilder builder)
    {
        super(builder);

        if(null == displayUrl)
        {
            this.displayUrl = "";
        }

        if(null == oauth)
        {
            this.oauth = newOAuthBean().build();
        }
    }

    public String getDisplayUrl()
    {
        return displayUrl;
    }

    public OAuthBean getOauth()
    {
        return oauth;
    }

    public static RemoteContainerCapabilityBeanBuilder newRemoteContainerBean()
    {
        return new RemoteContainerCapabilityBeanBuilder();
    }

    public static RemoteContainerCapabilityBeanBuilder newRemoteContainerBean(RemoteContainerCapabilityBean defaultBean)
    {
        return new RemoteContainerCapabilityBeanBuilder(defaultBean);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(displayUrl, oauth);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof RemoteContainerCapabilityBean))
        {
            return false;
        }
        else
        {
            final RemoteContainerCapabilityBean that = (RemoteContainerCapabilityBean) obj;
            return Objects.equal(displayUrl, that.displayUrl) &&
                    Objects.equal(oauth, that.oauth);
        }
    }
}
