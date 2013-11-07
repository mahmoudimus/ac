package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IFrameServletBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AddonUrlTemplatePair;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;

/**
 * Adapts a connect page bean into a web item and iframe servlet bean.
 */
public class ConnectPageCapabilityBeanAdapter // TODO: Shit name
{
    private final WebItemCapabilityBean webItemBean;
    private final IFrameServletBean servletBean;


    public ConnectPageCapabilityBeanAdapter(ConnectPageCapabilityBean pageBean, String pluginKey)
    {
        this(pageBean, pluginKey, "", "", ImmutableMap.<String, String>of(), null);
    }

    public ConnectPageCapabilityBeanAdapter(ConnectPageCapabilityBean pageBean, String pluginKey, String decorator, String templateSuffix,
                                            Map<String, String> metaTagsContent, Condition condition)
    {
        AddonUrlTemplatePair urlTemplatePair = createUrlTemplatePair(pageBean, pluginKey);

        // TODO: In ACDEV-396 push the url template into RemoteWebLink
        webItemBean = createWebItemCapabilityBean(pageBean, urlTemplatePair.getHostUrlPaths().getHostUrlTemplate().getTemplateString());
        servletBean = createServletBean(pageBean, urlTemplatePair, decorator, templateSuffix, metaTagsContent, condition);
    }

    private AddonUrlTemplatePair createUrlTemplatePair(ConnectPageCapabilityBean pageBean, String pluginKey)
    {
        return new AddonUrlTemplatePair(pageBean.getUrl(), pluginKey);
    }

    private WebItemCapabilityBean createWebItemCapabilityBean(ConnectPageCapabilityBean bean,
                                                              String localUrl)
    {
        return newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withLink(localUrl)
                .withLocation(bean.getAbsoluteLocation())
                .withWeight(bean.getWeight())
                .build();
    }

    private IFrameServletBean createServletBean(ConnectPageCapabilityBean pageBean, AddonUrlTemplatePair urlTemplatePair,
                                                String decorator, String templateSuffix, Map<String, String> metaTagsContent, Condition condition)
    {
        final String pageName = (!Strings.isNullOrEmpty(pageBean.getName().getValue()) ? pageBean.getName().getValue() : pageBean.getKey());
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
