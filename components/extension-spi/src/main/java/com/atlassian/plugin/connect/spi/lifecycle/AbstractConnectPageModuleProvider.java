package com.atlassian.plugin.connect.spi.lifecycle;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ConditionUtils;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.AddonUrlContext.page;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Base class for ConnectModuleProviders of Connect Pages. Note that there is actually no P2 module descriptor. Instead
 * it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageModuleProvider extends AbstractConnectModuleProvider<ConnectPageModuleBean> {
    private static final String RAW_CLASSIFIER = "raw";

    private final PluginRetrievalService pluginRetrievalService;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private ConditionClassAccessor conditionClassAccessor;
    private ConditionLoadingValidator conditionLoadingValidator;

    public AbstractConnectPageModuleProvider(PluginRetrievalService pluginRetrievalService,
                                             IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             ConditionClassAccessor conditionClassAccessor,
                                             ConditionLoadingValidator conditionLoadingValidator) {
        this.pluginRetrievalService = pluginRetrievalService;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.conditionClassAccessor = conditionClassAccessor;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
                                                                         ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<ConnectPageModuleBean> pages = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), pages);
        validateConditions(descriptor, pages);
        return pages;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectPageModuleBean> modules, ConnectAddonBean addon) {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (ConnectPageModuleBean bean : modules) {
            if (hasWebItem()) {
                // create a web item targeting the iframe page
                Integer weight = bean.getWeight() == null ? getDefaultWeight() : bean.getWeight();
                String location = isNullOrEmpty(bean.getLocation()) ? getDefaultSection() : bean.getLocation();

                WebItemModuleBean webItemBean = newWebItemBean()
                        .withName(bean.getName())
                        .withKey(bean.getRawKey())
                        .withContext(page)
                        .withUrl(ConnectIFrameServletPath.forModule(addon.getKey(), bean.getRawKey()))
                        .withLocation(location)
                        .withWeight(weight)
                        .withIcon(bean.getIcon())
                        .withConditions(bean.getConditions())
                        .setNeedsEscaping(needsEscaping())
                        .build();

                descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(
                        webItemBean, addon, pluginRetrievalService.getPlugin(), getConditionClasses()));
            }

            registerIframeRenderStrategy(bean, addon);
        }

        return descriptors;
    }

    protected void registerIframeRenderStrategy(ConnectPageModuleBean page, ConnectAddonBean connectAddonBean) {
        // register a render strategy for our iframe page
        IFrameRenderStrategy pageRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(connectAddonBean.getKey())
                .module(page.getKey(connectAddonBean))
                .pageTemplate()
                .urlTemplate(page.getUrl())
                .decorator(getDecorator())
                .conditions(page.getConditions())
                .conditionClasses(getConditionClasses())
                .title(page.getDisplayName())
                .resizeToParent(true)
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), page.getRawKey(), pageRenderStrategy);

        // and an additional strategy for raw content, in case the user wants to use it as a dialog target
        IFrameRenderStrategy rawRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(connectAddonBean.getKey())
                .module(page.getKey(connectAddonBean))
                .genericBodyTemplate()
                .urlTemplate(page.getUrl())
                .conditions(page.getConditions())
                .conditionClasses(getConditionClasses())
                .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                .build();
        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), page.getRawKey(), RAW_CLASSIFIER, rawRenderStrategy);
    }

    protected boolean needsEscaping() {
        return true;
    }

    protected Iterable<Class<? extends Condition>> getConditionClasses() {
        return Collections.emptyList();
    }

    protected boolean hasWebItem() {
        return true;
    }

    protected abstract String getDecorator();

    protected abstract String getDefaultSection();

    protected abstract int getDefaultWeight();

    protected void validateConditions(ShallowConnectAddonBean descriptor, List<ConnectPageModuleBean> pageBeans) throws ConnectModuleValidationException {
        for (ConnectPageModuleBean page : pageBeans) {
            for (SingleConditionBean condition : ConditionUtils.getSingleConditionsRecursively(page.getConditions())) {
                assertValidPageCondition(descriptor, condition);
            }
        }
    }

    private void assertValidPageCondition(ShallowConnectAddonBean descriptor, SingleConditionBean conditionBean) throws ConnectModuleValidationException {
        if (!isRemoteCondition(conditionBean.getCondition()) && !isContextFreeCondition(conditionBean)) {
            String exceptionMessage = String.format("The add-on includes a Page Module with an unsupported condition (%s)", conditionBean.getCondition());
            throw new ConnectModuleValidationException(descriptor, getMeta(), exceptionMessage,
                    "connect.install.error.page.with.invalid.condition", conditionBean.getCondition());
        }
    }

    private boolean isContextFreeCondition(SingleConditionBean conditionBean) {
        return conditionClassAccessor.getConditionClassForNoContext(conditionBean).isPresent();
    }
}
