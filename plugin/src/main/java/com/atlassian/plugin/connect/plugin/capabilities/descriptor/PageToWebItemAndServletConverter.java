package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.UrlTemplate;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AddOnUrlContext.product;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Creates a web item bean and an iFrame servlet bean from a page bean.
 * As there are no P2 implementations of pages they are implemented with a web item plus servlet
 */
public class PageToWebItemAndServletConverter
{
    private final WebItemModuleBean webItemBean;
    private final IFrameServletBean servletBean;
    private final static Condition DEFAULT_CONDITION = new AlwaysDisplayCondition();


    public PageToWebItemAndServletConverter(ConnectPageModuleBean pageBean, String pluginKey,
                                            int defaultWeight, String defaultSection, String decorator,
                                            String templateSuffix, Map<String, String> metaTagsContent,
                                            Condition condition, IFrameParams iFrameParams)
    {
        AddonUrlTemplatePair urlTemplatePair = createUrlTemplatePair(pageBean, pluginKey);

        // TODO: In ACDEV-498 push the url template into RemoteWebLink
        webItemBean = createWebItemModuleBean(pageBean, urlTemplatePair.getHostUrlPaths().getHostUrlTemplate(),
                defaultWeight, defaultSection);
        servletBean = createServletBean(pageBean, urlTemplatePair, decorator, templateSuffix, metaTagsContent,
                condition, iFrameParams);
    }

    private AddonUrlTemplatePair createUrlTemplatePair(ConnectPageModuleBean pageBean, String pluginKey)
    {
        return new AddonUrlTemplatePair(pageBean.getUrl(), pluginKey);
    }

    private WebItemModuleBean createWebItemModuleBean(ConnectPageModuleBean bean, UrlTemplate hostUrlTemplate,
            int defaultWeight, String defaultSection)
    {
        Integer weight = bean.getWeight() == null ? defaultWeight : bean.getWeight();
        String location = isNullOrEmpty(bean.getLocation()) ? defaultSection : bean.getLocation();

        return newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withContext(product) // Note the reason this is not 'addon' AddonUrlTemplatePair encapsulates the knowledge about the relationship of the host wrapping url to the addon url
                .withUrl(hostUrlTemplate.getTemplateString())
                .withLocation(location)
                .withWeight(weight)
                .withIcon(bean.getIcon())
                .withConditions(bean.getConditions())
                .build();
    }

    private IFrameServletBean createServletBean(ConnectPageModuleBean pageBean, AddonUrlTemplatePair urlTemplatePair,
                                                String decorator, String templateSuffix, Map<String, String> metaTagsContent,
                                                Condition condition, IFrameParams iFrameParams)
    {
        PageInfo pageInfo = new PageInfo(decorator, templateSuffix, pageBean.getDisplayName(),
                condition == null ? DEFAULT_CONDITION : condition, metaTagsContent);

        return new IFrameServletBean(pageBean, urlTemplatePair, pageInfo,
                iFrameParams == null ? new IFrameParamsImpl() : iFrameParams);
    }

    public WebItemModuleBean getWebItemBean()
    {
        return webItemBean;
    }

    public IFrameServletBean getServletBean()
    {
        return servletBean;
    }
}
