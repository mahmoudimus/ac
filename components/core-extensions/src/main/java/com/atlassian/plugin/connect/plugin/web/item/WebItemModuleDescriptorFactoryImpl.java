package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.spi.web.item.ProductSpecificWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
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

import static com.atlassian.plugin.connect.api.util.Dom4jUtils.printNode;
import static com.google.common.collect.Lists.newArrayList;

@Component
@ExportAsDevService
public class WebItemModuleDescriptorFactoryImpl implements WebItemModuleDescriptorFactory {

    private static final Logger log = LoggerFactory.getLogger(WebItemModuleDescriptorFactoryImpl.class);

    private final ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory;

    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final WebFragmentLocationQualifier webFragmentLocationQualifier;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public WebItemModuleDescriptorFactoryImpl(ProductSpecificWebItemModuleDescriptorFactory productWebItemDescriptorFactory,
                                              IconModuleFragmentFactory iconModuleFragmentFactory,
                                              WebFragmentLocationQualifier webFragmentLocationQualifier,
                                              ConditionModuleFragmentFactory conditionModuleFragmentFactory) {
        this.productWebItemDescriptorFactory = productWebItemDescriptorFactory;
        this.iconModuleFragmentFactory = iconModuleFragmentFactory;
        this.webFragmentLocationQualifier = webFragmentLocationQualifier;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(WebItemModuleBean bean, ConnectAddonBean addon, Plugin plugin) {
        return createModuleDescriptor(bean, addon, plugin, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(WebItemModuleBean bean, ConnectAddonBean addon, Plugin plugin,
                                                          Class<? extends Condition> additionalCondition) {
        return createModuleDescriptor(bean, addon, plugin, Collections.<Class<? extends Condition>>singletonList(additionalCondition)
        );
    }

    @Override
    public WebItemModuleDescriptor createModuleDescriptor(WebItemModuleBean bean, ConnectAddonBean addon, Plugin plugin,
                                                          Iterable<Class<? extends Condition>> additionalConditions) {
        Element webItemElement = new DOMElement("web-item");

        String webItemKey = bean.getKey(addon);

        String webItemLabel = bean.needsEscaping() ? StringEscapeUtils.escapeHtml(bean.getName().getValue()) : bean.getName().getValue();
        String i18nKey = bean.needsEscaping() ? StringEscapeUtils.escapeHtml(bean.getName().getI18n()) : bean.getName().getI18n();

        webItemElement.addAttribute("key", webItemKey);

        String section = webFragmentLocationQualifier.processLocation(bean.getLocation(), addon);
        webItemElement.addAttribute("section", section);

        webItemElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        webItemElement.addElement("label")
                .addAttribute("key", i18nKey)
                .setText(webItemLabel);

        if (null != bean.getTooltip()) {
            webItemElement.addElement("tooltip")
                    .addAttribute("key", bean.getTooltip().getI18n())
                    .setText(bean.getTooltip().getValue());
        }

        String linkId = addon.getKey() + "-" + webItemKey;
        Element linkElement = webItemElement.addElement("link").addAttribute("linkId", linkId);
        String url = bean.getUrl();
        linkElement.setText(url);

        List<String> styles = newArrayList(bean.getStyleClasses());

        if (null != bean.getIcon()) {
            webItemElement.add(iconModuleFragmentFactory.createFragment(addon.getKey(), bean.getIcon()));
        }

        if (!bean.getConditions().isEmpty()) {
            webItemElement.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions(),
                    additionalConditions));
        }

        if (bean.getTarget().isDialogTarget()) {
            styles.add("ap-dialog");
        } else if (bean.getTarget().isInlineDialogTarget()) {
            styles.add("ap-inline-dialog");
        }

        if (!bean.getTarget().isPageTarget()) {
            styles.add("ap-plugin-key-" + addon.getKey());
            styles.add("ap-module-key-" + webItemKey);
        }

        final WebItemTargetOptions options = bean.getTarget().getOptions();

        // use gson to turn it into a map
        final Gson gson = ConnectModulesGsonFactory.getGson();
        final Map<String, Object> dialogOptions = getDialogOptions(options, gson);
        Map<String, String> beanParams = bean.getParams();

        if (null != dialogOptions && !dialogOptions.isEmpty()) {
            //TODO: use regex to escape special characters with \
            for (Map.Entry<String, Object> entry : dialogOptions.entrySet()) {
                beanParams.put(DIALOG_OPTION_PREFIX + entry.getKey(), entry.getValue().toString());
            }
        }

        final boolean isDialog = bean.getTarget().isDialogTarget() || bean.getTarget().isInlineDialogTarget();

        for (Map.Entry<String, String> entry : bean.getParams().entrySet()) {
            webItemElement.addElement("param")
                    .addAttribute("name", entry.getKey())
                    .addAttribute("value", entry.getValue());
        }

        if (!styles.isEmpty()) {
            webItemElement.addElement("styleClass").setText(Joiner.on(" ").join(styles));
        }

        if (log.isDebugEnabled()) {
            log.debug("Created web item: " + printNode(webItemElement));
        }

        return createWebItemDescriptor(addon, plugin, webItemElement, webItemKey, url, bean.isAbsolute(), bean.getContext(), isDialog, section);
    }

    private WebItemModuleDescriptor createWebItemDescriptor(ConnectAddonBean addon, Plugin plugin, Element webItemElement, String moduleKey, String url,
                                                            boolean absolute, AddonUrlContext urlContext, boolean isDialog, String section) {
        webItemElement.addAttribute("system", "true");

        final WebItemModuleDescriptor descriptor = productWebItemDescriptorFactory.createWebItemModuleDescriptor(
                url
                , addon.getKey()
                , moduleKey
                , absolute
                , urlContext
                , isDialog
                , section);

        descriptor.init(plugin, webItemElement);

        return descriptor;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDialogOptions(WebItemTargetOptions options, Gson gson) {
        return gson.fromJson(gson.toJsonTree(options), Map.class);
    }
}
