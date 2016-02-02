package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectDataBuilderFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.DialogModuleBean;
import com.atlassian.plugin.connect.modules.beans.DialogModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.WebItemTargetOptions;
import com.atlassian.plugin.connect.plugin.AbstractConnectCoreModuleProvider;
import com.atlassian.plugin.connect.plugin.web.dialog.DialogOptionsValidator;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetType.dialog;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component
@ExportAsDevService
public class WebItemModuleProviderImpl extends AbstractConnectCoreModuleProvider<WebItemModuleBean> implements WebItemModuleProvider
{

    private static final WebItemModuleMeta META = new WebItemModuleMeta();

    private static final String DEFAULT_DIALOG_DIMENSION = "100%"; // NB: the client (js) may size the parent of the iframe if the opening is done from JS

    private final WebItemModuleDescriptorFactory webItemFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final WebFragmentLocationBlacklist webFragmentLocationBlacklist;
    private final ConditionClassAccessor conditionClassAccessor;
    private final ConditionLoadingValidator conditionLoadingValidator;
    private final RedirectRegistry redirectRegistry;
    private final RedirectDataBuilderFactory redirectDataBuilderFactory;

    @Autowired
    public WebItemModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WebItemModuleDescriptorFactory webItemFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebFragmentLocationBlacklist webFragmentLocationBlacklist,
            ConditionClassAccessor conditionClassAccessor,
            ConditionLoadingValidator conditionLoadingValidator,
            RedirectRegistry redirectRegistry,
            RedirectDataBuilderFactory redirectDataBuilderFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webItemFactory = webItemFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.webFragmentLocationBlacklist = webFragmentLocationBlacklist;
        this.conditionClassAccessor = conditionClassAccessor;
        this.conditionLoadingValidator = conditionLoadingValidator;
        this.redirectRegistry = redirectRegistry;
        this.redirectDataBuilderFactory = redirectDataBuilderFactory;
    }

    @Override
    public ConnectModuleMeta<WebItemModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<WebItemModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        final List<WebItemModuleBean> webItems = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        assertLocationNotBlacklisted(descriptor, webItems);
        validateUrls(descriptor, webItems);
        // TODO ACDEV-2430 Uncomment this when confident that it won't break existing plugins.
        // validateTargetOptions(descriptor, webItems);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), webItems);
        return webItems;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebItemModuleBean> modules, ConnectAddonBean addon)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebItemModuleBean bean : modules)
        {
            WebItemModuleBean resolvedBean = resolveBean(bean, addon);
            descriptors.add(beanToDescriptor(addon, pluginRetrievalService.getPlugin(), resolvedBean));
            registerIframeRenderStrategy(resolvedBean, addon);
        }
        return descriptors;
    }

    // WebItems with a dialog target specified by key will inherit properties from the dialog.
    private WebItemModuleBean resolveBean(WebItemModuleBean bean, ConnectAddonBean addon)
    {
        WebItemTargetBean target = bean.getTarget();
        String targetKey = target.getKey();
        if (!target.isDialogTarget() || isBlank(targetKey))
        {
            // The bean doesn't need resolving.
            return bean;
        }

        // Options and Url will be declared within the linked dialog module
        DialogModuleBean targetDialog = getTargetDialog(targetKey, addon);

        WebItemTargetBean resolvedTarget = newWebItemTargetBean(target)
                .withOptions(targetDialog.getOptions())
                .build();

        return newWebItemBean(bean)
                .withTarget(resolvedTarget)
                .withUrl(targetDialog.getUrl())
                .build();
    }

    private DialogModuleBean getTargetDialog(String targetKey, ConnectAddonBean addon)
    {
        Optional<List<ModuleBean>> dialogs = addon.getModules().getValidModuleListOfType(new DialogModuleMeta().getDescriptorKey(), e -> {});
        if (dialogs.isPresent())
        {
            Optional<ModuleBean> foundBean = dialogs.get().stream().filter(moduleBean ->
            {
                DialogModuleBean dialogBean = (DialogModuleBean) moduleBean;
                return dialogBean.getRawKey().equals(targetKey);
            }).findFirst();
            if (!foundBean.isPresent())
            {
                // This target's key points to a non-existent module.
                // TODO - refactor this throw to not suck. dT
                throw new IllegalArgumentException("Unknown dialog module key: " + targetKey);
            }
            return (DialogModuleBean) foundBean.get();
        }
        else
        {
            // TODO - refactor this throw to not suck either. dT
            throw new IllegalArgumentException("No dialog modules?");
        }
    }

    @VisibleForTesting
    void assertLocationNotBlacklisted(ShallowConnectAddonBean descriptor, List<WebItemModuleBean> webItemModuleBeans)
            throws ConnectModuleValidationException
    {
        List<String> blacklistedLocationsUsed = webItemModuleBeans.stream()
                .filter(new Predicate<WebItemModuleBean>()
                {
                    @Override
                    public boolean test(WebItemModuleBean webItem)
                    {
                        return webFragmentLocationBlacklist.getBlacklistedWebItemLocations().contains(webItem.getLocation());
                    }
                })
                .map(new Function<WebItemModuleBean, String>()
                {
                    @Override
                    public String apply(WebItemModuleBean webItemModuleBean)
                    {
                        return webItemModuleBean.getLocation();
                    }
                })
                .collect(toList());

        if (blacklistedLocationsUsed.size() > 0)
        {
            final String exceptionMsg = String.format("Installation failed. The add-on includes a web fragment with an unsupported location (%s).", blacklistedLocationsUsed);
            throw new ConnectModuleValidationException(descriptor, getMeta(), exceptionMsg, "connect.install.error.invalid.location", blacklistedLocationsUsed.toArray(new String[blacklistedLocationsUsed.size()]));
        }
    }

    @VisibleForTesting
    void validateUrls(ShallowConnectAddonBean descriptor, List<WebItemModuleBean> webItems)
            throws ConnectModuleValidationException
    {
        List<String> invalidItemKeys = webItems.stream()
                .filter(webItem -> {
                    if (isNotBlank(webItem.getUrl()))
                        return false;  // this one's fine

                    // All web-items have urls UNLESS they target another bean (which has a required url)
                    WebItemTargetType type = webItem.getTarget().getType();
                    String key = webItem.getTarget().getKey();

                    // If type was dialog and key was set we could rely on other validation to catch any missing
                    // url in the target dialog module.
                    return !type.equals(dialog) || isBlank(key);
                })
                .map(RequiredKeyBean::getRawKey)
                .collect(toList());

        if (!invalidItemKeys.isEmpty())
        {
            String exceptionMsg = String.format("Installation failed. The add-on includes web-items (%s) with no url.", invalidItemKeys);
            throw new ConnectModuleValidationException(descriptor, getMeta(), exceptionMsg, "connect.install.error.missing.url", invalidItemKeys.toArray(new String[invalidItemKeys.size()]));
        }
    }

    private void validateTargetOptions(ShallowConnectAddonBean descriptor, List<WebItemModuleBean> webItems)
            throws ConnectModuleValidationException
    {
        DialogOptionsValidator validator = new DialogOptionsValidator(descriptor, getMeta());
        for (WebItemModuleBean webItem : webItems) {
            WebItemTargetOptions options = webItem.getTarget().getOptions();
            if (options instanceof DialogOptions)
                validator.validate((DialogOptions) options);
        }
    }

    private ModuleDescriptor beanToDescriptor(ConnectAddonBean addon, Plugin plugin, WebItemModuleBean bean)
    {
        ModuleDescriptor descriptor;

        final WebItemTargetBean target = bean.getTarget();
        if (requiredRedirection(bean))
        {
            RedirectData redirectData = redirectDataBuilderFactory.builder()
                    .addOn(addon.getKey())
                    .urlTemplate(bean.getUrl())
                    .accessDeniedTemplateType(RedirectData.AccessDeniedTemplateType.PAGE)
                    .conditions(filterProductSpecificConditions(bean.getConditions()))
                    .build();

            redirectRegistry.register(addon.getKey(), bean.getRawKey(), redirectData);
        }

        if (bean.isAbsolute() ||
            bean.getContext().equals(AddonUrlContext.product) ||
            bean.getContext().equals(AddonUrlContext.addon) && !target.isDialogTarget() && !target.isInlineDialogTarget())
        {
            descriptor = webItemFactory.createModuleDescriptor(bean, addon, plugin);
        }
        else
        {
            String localUrl = ConnectIFrameServletPath.forModule(addon.getKey(), bean.getUrl());

            WebItemModuleBean newBean = newWebItemBean(bean).withUrl(localUrl).build();
            descriptor = webItemFactory.createModuleDescriptor(newBean, addon, plugin);
        }

        return descriptor;
    }

    private boolean requiredRedirection(final WebItemModuleBean bean)
    {
        // Link to the add-ons may require revalidation of JWT token so they need to do request though redirect servlet.
        // Absolute links points to the external servers like wikipedia, so they do not need to be signed, so they do not need go through redirect servlet.
        return !bean.isAbsolute() && bean.getContext().equals(AddonUrlContext.addon) && bean.getTarget().getType().equals(WebItemTargetType.page);
    }

    private void registerIframeRenderStrategy(WebItemModuleBean webItem, ConnectAddonBean descriptor)
    {
        // Allow a web item which opens in a dialog to be opened programmatically, too
        final WebItemTargetBean target = webItem.getTarget();
        if (target.isDialogTarget() || target.isInlineDialogTarget())
        {
            List<ConditionalBean> iframeConditions = filterProductSpecificConditions(webItem.getConditions());
            final IFrameRenderStrategy iFrameRenderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addon(descriptor.getKey())
                    .module(webItem.getKey(descriptor))
                    .genericBodyTemplate()
                    .urlTemplate(webItem.getUrl())
                    .title(webItem.getDisplayName())
                    .conditions(iframeConditions)
                    .dimensions(DEFAULT_DIALOG_DIMENSION, DEFAULT_DIALOG_DIMENSION) // the client (js) will size the parent of the iframe
                    .dialog(target.isDialogTarget())
                    .sign(!webItem.getUrl().toLowerCase().startsWith("http")) // don't sign requests to arbitrary URLs (e.g. wikipedia)
                    .build();

            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getKey(descriptor), iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getRawKey(), IFrameRenderStrategyRegistry.RAW_CLASSIFIER, iFrameRenderStrategy);
            iFrameRenderStrategyRegistry.register(descriptor.getKey(), webItem.getRawKey(), iFrameRenderStrategy);
        }
    }

    @VisibleForTesting
    List<ConditionalBean> filterProductSpecificConditions(List<ConditionalBean> conditions)
    {
        return filterSingleConditionsRecursively(conditions, new Predicate<SingleConditionBean>()
        {

            @Override
            public boolean test(SingleConditionBean conditionalBean)
            {
                return conditionClassAccessor.getConditionClassForNoContext(conditionalBean).isPresent();
            }
        });
    }

    private List<ConditionalBean> filterSingleConditionsRecursively(List<ConditionalBean> conditions,
            Predicate<SingleConditionBean> filterPredicate)
    {
        List<ConditionalBean> filteredConditions = new ArrayList<>();
        for (ConditionalBean condition : conditions)
        {
            if (SingleConditionBean.class.isAssignableFrom(condition.getClass()))
            {
                if (filterPredicate.test((SingleConditionBean) condition))
                {
                    filteredConditions.add(condition);
                }
            }
            else
            {
                CompositeConditionBean compositeCondition = (CompositeConditionBean) condition;
                List<ConditionalBean> filteredNestedConditions = filterSingleConditionsRecursively(
                        compositeCondition.getConditions(), filterPredicate);
                if (!filteredNestedConditions.isEmpty())
                {
                    filteredConditions.add(CompositeConditionBean.newCompositeConditionBean(compositeCondition)
                            .withConditions(filteredNestedConditions)
                            .build());
                }
            }
        }
        return filteredConditions;
    }
}
