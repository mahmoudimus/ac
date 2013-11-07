package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;

public class IFrameServletBean
{
    private final NameToKeyBean linkBean;
    private final AddonUrlTemplatePair urlTemplatePair;

    private final PageInfo pageInfo;

    public IFrameServletBean(final NameToKeyBean bean,
                      final AddonUrlTemplatePair urlTemplatePair,
                      final PageInfo pageInfo)
    {
        this.linkBean = bean;
        this.urlTemplatePair = urlTemplatePair;
        this.pageInfo = pageInfo;
    }

    public NameToKeyBean getLinkBean()
    {
        return linkBean;
    }

    public AddonUrlTemplatePair getUrlTemplatePair()
    {
        return urlTemplatePair;
    }

    public PageInfo getPageInfo()
    {
        return pageInfo;
    }

}
