package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.UrlTemplate;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Creates a web item bean and a iFrame servlet bean from a page bean.
 */
public class PageToWebItemAndServletConverter
{
    private final WebItemCapabilityBean webItemBean;
    private final IFrameServletBean servletBean;
    private final static Condition DEFAULT_CONDITION = new AlwaysDisplayCondition();


    public PageToWebItemAndServletConverter(ConnectPageCapabilityBean pageBean, String pluginKey,
                                            ProductAccessor productAccessor, String decorator, String templateSuffix,
                                            Map<String, String> metaTagsContent, Condition condition,
                                            IFrameParams iFrameParams)
    {
        AddonUrlTemplatePair urlTemplatePair = createUrlTemplatePair(pageBean, pluginKey);

        // TODO: In ACDEV-396 push the url template into RemoteWebLink
        webItemBean = createWebItemCapabilityBean(pageBean, urlTemplatePair.getHostUrlPaths().getHostUrlTemplate(), productAccessor);
        servletBean = createServletBean(pageBean, urlTemplatePair, decorator, templateSuffix, metaTagsContent,
                condition, iFrameParams);
    }

    private AddonUrlTemplatePair createUrlTemplatePair(ConnectPageCapabilityBean pageBean, String pluginKey)
    {
        return new AddonUrlTemplatePair(pageBean.getUrl(), pluginKey);
    }

    private WebItemCapabilityBean createWebItemCapabilityBean(ConnectPageCapabilityBean bean, UrlTemplate hostUrlTemplate,
                                                              ProductAccessor productAccessor)
    {
        Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
        String location = isNullOrEmpty(bean.getLocation()) ? productAccessor.getPreferredGeneralSectionKey() : bean.getLocation();

        return newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withLink(hostUrlTemplate.getTemplateString())
                .withLocation(location)
                .withWeight(weight)
                .withIcon(bean.getIcon())
                .build();
    }

    private IFrameServletBean createServletBean(ConnectPageCapabilityBean pageBean, AddonUrlTemplatePair urlTemplatePair,
                                                String decorator, String templateSuffix, Map<String, String> metaTagsContent,
                                                Condition condition, IFrameParams iFrameParams)
    {
        PageInfo pageInfo = new PageInfo(decorator, templateSuffix, pageBean.getDisplayName(),
                condition == null ? DEFAULT_CONDITION : condition, metaTagsContent);

        return new IFrameServletBean(pageBean, urlTemplatePair, pageInfo,
                iFrameParams == null ? new IFrameParamsImpl() : iFrameParams);
    }

    public WebItemCapabilityBean getWebItemBean()
    {
        return webItemBean;
    }

    public IFrameServletBean getServletBean()
    {
        return servletBean;
    }
}
