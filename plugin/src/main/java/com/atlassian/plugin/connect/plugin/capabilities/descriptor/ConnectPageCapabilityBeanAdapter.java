package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.UrlTemplate;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Adapts a connect page bean into a web item and iframe servlet bean.
 */
public class ConnectPageCapabilityBeanAdapter // TODO: Shit name
{
    private final WebItemCapabilityBean webItemBean;
    private final IFrameServletBean servletBean;
    private final static Condition DEFAULT_CONDITION = new AlwaysDisplayCondition();


    public ConnectPageCapabilityBeanAdapter(ConnectPageCapabilityBean pageBean, String pluginKey,
                                            ProductAccessor productAccessor, String decorator, String templateSuffix,
                                            Map<String, String> metaTagsContent, Condition condition)
    {
        AddonUrlTemplatePair urlTemplatePair = createUrlTemplatePair(pageBean, pluginKey);

        // TODO: In ACDEV-396 push the url template into RemoteWebLink
        webItemBean = createWebItemCapabilityBean(pageBean, urlTemplatePair.getHostUrlPaths().getHostUrlTemplate(), productAccessor);
        servletBean = createServletBean(pageBean, urlTemplatePair, decorator, templateSuffix, metaTagsContent,
                condition == null ? DEFAULT_CONDITION : condition);
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
                .build();
    }

    private IFrameServletBean createServletBean(ConnectPageCapabilityBean pageBean, AddonUrlTemplatePair urlTemplatePair,
                                                String decorator, String templateSuffix, Map<String, String> metaTagsContent,
                                                Condition condition)
    {
        final String pageName = (!isNullOrEmpty(pageBean.getName().getValue()) ? pageBean.getName().getValue() : pageBean.getKey());
        PageInfo pageInfo = new PageInfo(decorator, templateSuffix, pageName, condition, metaTagsContent);
        return new IFrameServletBean(pageBean, urlTemplatePair, pageInfo);
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
