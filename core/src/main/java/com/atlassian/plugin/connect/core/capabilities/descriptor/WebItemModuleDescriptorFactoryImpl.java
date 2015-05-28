package com.atlassian.plugin.connect.core.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.collect.Lists.newArrayList;


@Component
public class WebItemModuleDescriptorFactoryImpl implements WebItemModuleDescriptorFactory
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactoryImpl.class);

    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;

    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public WebItemModuleDescriptorFactoryImpl(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory,
                                          IconModuleFragmentFactory iconModuleFragmentFactory,
                                          ConditionModuleFragmentFactory conditionModuleFragmentFactory,
                                          ParamsModuleFragmentFactory paramsModuleFragmentFactory)
    {
        this.productWebItemDescriptorFactory = productWebItemDescriptorFactory;
        this.iconModuleFragmentFactory = iconModuleFragmentFactory;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext,
                                                          Plugin theConnectPlugin, WebItemModuleBean bean)
    {
        return createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
                                                          WebItemModuleBean bean, Class<? extends Condition> additionalCondition)
    {
        return createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean,
                Collections.<Class<? extends Condition>>singletonList(additionalCondition));
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin,
                                                          WebItemModuleBean bean, Iterable<Class<? extends Condition>> additionalConditions)
    {
        Element webItemElement = new DOMElement("web-item");

        final ConnectAddonBean addon = moduleProviderContext.getConnectAddonBean();

        String webItemKey = bean.getKey(addon);

        String webItemLabel = bean.needsEscaping() ? StringEscapeUtils.escapeHtml(bean.getName().getValue()) : bean.getName().getValue();
        String i18nKey = bean.needsEscaping() ? StringEscapeUtils.escapeHtml(bean.getName().getI18n()) : bean.getName().getI18n();

        webItemElement.addAttribute("key", webItemKey);

        String section = moduleProviderContext.getLocationQualifier().processLocation(bean.getLocation());
        webItemElement.addAttribute("section", section);

        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        webItemElement.addElement("label")
                .addAttribute("key", i18nKey)
                .setText(webItemLabel);

        if (null != bean.getTooltip())
        {
            webItemElement.addElement("tooltip")
                    .addAttribute("key", bean.getTooltip().getI18n())
                    .setText(bean.getTooltip().getValue());
        }

        String linkId = addon.getKey() + "-" + webItemKey;
        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", linkId);
        String url = bean.getUrl();
        linkElement.setText(url);

        List<String> styles = newArrayList(bean.getStyleClasses());

        if (null != bean.getIcon())
        {
            webItemElement.add(iconModuleFragmentFactory.createFragment(addon.getKey(), bean.getIcon()));
        }

        if (!bean.getConditions().isEmpty())
        {
            webItemElement.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions(),
                    additionalConditions));
        }

        if (bean.getTarget().isDialogTarget())
        {
            styles.add("ap-dialog");
        }
        else if (bean.getTarget().isInlineDialogTarget())
        {
            styles.add("ap-inline-dialog");
        }

        if (!bean.getTarget().isPageTarget())
        {
            styles.add("ap-plugin-key-" + addon.getKey());
            styles.add("ap-module-key-" + webItemKey);
        }

        final WebItemTargetOptions options = bean.getTarget().getOptions();

        // use gson to turn it into a map
        final Gson gson = ConnectModulesGsonFactory.getGson();
        final Map<String, Object> dialogOptions = gson.fromJson(gson.toJsonTree(options), Map.class);
        Map<String, String> beanParams = bean.getParams();

        if (null != dialogOptions && !dialogOptions.isEmpty())
        {
            //TODO: use regex to escape special characters with \
            for (Map.Entry<String, Object> entry : dialogOptions.entrySet())
            {
                beanParams.put(DIALOG_OPTION_PREFIX + entry.getKey(), entry.getValue().toString());
            }
        }

        final boolean isDialog = bean.getTarget().isDialogTarget() || bean.getTarget().isInlineDialogTarget();

        paramsModuleFragmentFactory.addParamsToElement(webItemElement, bean.getParams());

        if (!styles.isEmpty())
        {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(styles));
        }

        if (log.isDebugEnabled())
        {
            log.debug("Created web item: " + printNode(webItemElement));
        }

        return createWebItemDescriptor(addon, theConnectPlugin, webItemElement, webItemKey, url, bean.isAbsolute(), bean.getContext(), isDialog, section);
    }

    private WebItemModuleDescriptor createWebItemDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, Element webItemElement, String moduleKey, String url,
                                                            boolean absolute, AddOnUrlContext urlContext, boolean isDialog, String section)
    {
        webItemElement.addAttribute("system", "true");

        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory.createWebItemModuleDescriptor(
                url
                , addon.getKey()
                , moduleKey
                , absolute
                , urlContext
                , isDialog
                , section);

        descriptor.init(theConnectPlugin, webItemElement);

        return descriptor;
    }
}
