package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.module.webitem.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.base.Joiner;
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
public class WebItemModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<WebItemModuleBean, WebItemModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactory.class);
    public static final String DIALOG_OPTION_PREFIX = "-acopt-";

    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;

    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;

    @Autowired
    public WebItemModuleDescriptorFactory(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory,
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
    public WebItemModuleDescriptor createModuleDescriptor(Plugin plugin, WebItemModuleBean bean)
    {
        return createModuleDescriptor(plugin, bean, Collections.<Class<? extends Condition>>emptyList());
    }

    public WebItemModuleDescriptor createModuleDescriptor(Plugin plugin, WebItemModuleBean bean, Class<? extends Condition> additionalCondition)
    {
        return createModuleDescriptor(plugin, bean, Collections.<Class<? extends Condition>>singletonList(additionalCondition));
    }

    public WebItemModuleDescriptor createModuleDescriptor(Plugin plugin, WebItemModuleBean bean, Iterable<Class<? extends Condition>> additionalConditions)
    {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = bean.getKey();

        webItemElement.addAttribute("key", webItemKey);
        webItemElement.addAttribute("section", bean.getLocation());
        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        webItemElement.addElement("label")
                .addAttribute("key", bean.getName().getI18n())
                .setText(bean.getName().getValue());

        if (null != bean.getTooltip())
        {
            webItemElement.addElement("tooltip")
                    .addAttribute("key", bean.getTooltip().getI18n())
                    .setText(bean.getTooltip().getValue());
        }

        String linkId = plugin.getKey() + "-" + webItemKey;
        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", linkId);
        String url = bean.getUrl();
        linkElement.setText(url);

        List<String> styles = newArrayList(bean.getStyleClasses());

        if (null != bean.getIcon())
        {
            webItemElement.add(iconModuleFragmentFactory.createFragment(plugin.getKey(), bean.getIcon()));
        }

        if (!bean.getConditions().isEmpty())
        {
            webItemElement.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(), bean.getConditions(),
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
            styles.add("ap-plugin-key-" + plugin.getKey());
            styles.add("ap-module-key-" + webItemKey);
        }

        Map<String, Object> dialogOptions = bean.getTarget().getOptions();
        Map<String, String> beanParams = bean.getParams();

        if (null != dialogOptions && !dialogOptions.isEmpty())
        {
            //TODO: use regex to escape special characters with \
            for (Map.Entry<String, Object> entry : dialogOptions.entrySet())
            {
                beanParams.put(DIALOG_OPTION_PREFIX + entry.getKey(), entry.getValue().toString());
            }
        }

        final boolean isDialog = bean.getTarget().isDialogTarget();

        paramsModuleFragmentFactory.addParamsToElement(webItemElement,bean.getParams());

        if (!styles.isEmpty())
        {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(styles));
        }

        if (log.isDebugEnabled())
        {
            log.debug("Created web item: " + printNode(webItemElement));
        }

        return createWebItemDescriptor(plugin, webItemElement, webItemKey, url, bean.isAbsolute(), bean.getContext(), isDialog);
    }

    private WebItemModuleDescriptor createWebItemDescriptor(Plugin plugin, Element webItemElement, String moduleKey, String url,
                                                            boolean absolute, AddOnUrlContext urlContext, boolean isDialog)
    {
        webItemElement.addAttribute("system", "true");

        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory.createWebItemModuleDescriptor(
                url
                , plugin.getKey()
                , moduleKey
                , absolute
                , urlContext
                , isDialog);

        descriptor.init(plugin, webItemElement);

        return descriptor;
    }

}
