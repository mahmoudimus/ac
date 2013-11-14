package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.plugin.connect.plugin.capabilities.beans.NameToKeyBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameParams;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The data model for an IFrame servlet
 */
public class IFrameServletBean
{
    private final NameToKeyBean linkBean;
    private final AddonUrlTemplatePair urlTemplatePair;
    private final PageInfo pageInfo;
    private final IFrameParams iFrameParams;

    public IFrameServletBean(final NameToKeyBean bean,
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

    public IFrameParams getiFrameParams()
    {
        return iFrameParams;
    }
}
