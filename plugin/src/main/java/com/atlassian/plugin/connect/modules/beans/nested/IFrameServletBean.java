package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.GeneratedKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameParams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The data model for an IFrame servlet
 */
public class IFrameServletBean
{
    private final GeneratedKeyBean linkBean;
    private final AddonUrlTemplatePair urlTemplatePair;
    private final PageInfo pageInfo;
    private final IFrameParams iFrameParams;

    public IFrameServletBean(final GeneratedKeyBean bean,
                             final AddonUrlTemplatePair urlTemplatePair,
                             final PageInfo pageInfo, IFrameParams iFrameParams)
    {
        this.linkBean = checkNotNull(bean);
        this.urlTemplatePair = checkNotNull(urlTemplatePair);
        this.pageInfo = checkNotNull(pageInfo);
        this.iFrameParams = checkNotNull(iFrameParams);
    }

    /**
     * The bean that holds the link which targets the servlet
     */
    public GeneratedKeyBean getLinkBean()
    {
        return linkBean;
    }

    /**
     * The pair of UrlTemplates that the servlet supports
     */
    public AddonUrlTemplatePair getUrlTemplatePair()
    {
        return urlTemplatePair;
    }

    /**
     * The page information for the add on page that the servlet supports
     */
    public PageInfo getPageInfo()
    {
        return pageInfo;
    }

    /**
     * Parameters for the iFrame that hosts the add on
     */
    public IFrameParams getiFrameParams()
    {
        return iFrameParams;
    }
}
