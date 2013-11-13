package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;

/**
 * The data model for an IFrame servlet
 */
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

    /**
     * The bean that holds the link which targets the servlet
     *
     * @return
     */
    public NameToKeyBean getLinkBean()
    {
        return linkBean;
    }

    /**
     * The pair of UrlTemplates that the servlet supports
     *
     * @return
     */
    public AddonUrlTemplatePair getUrlTemplatePair()
    {
        return urlTemplatePair;
    }

    /**
     * The page information for the add on page that the servlet supports
     *
     * @return
     */
    public PageInfo getPageInfo()
    {
        return pageInfo;
    }

}
