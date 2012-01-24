package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyMap;

/**
 * Generates application-type modules
 */
@Component
public class ApplicationTypeModuleGenerator implements RemoteModuleGenerator
{
    private final MutatingApplicationLinkService mutatingApplicationLinkService;
    private final PermissionManager permissionManager;
    private final ApplicationTypeClassLoader applicationTypeClassLoader;

    @Autowired
    public ApplicationTypeModuleGenerator(MutatingApplicationLinkService mutatingApplicationLinkService,
                                          PermissionManager permissionManager,
                                          ApplicationTypeClassLoader applicationTypeClassLoader)
    {
        this.mutatingApplicationLinkService = mutatingApplicationLinkService;
        this.permissionManager = permissionManager;
        this.applicationTypeClassLoader = applicationTypeClassLoader;
    }

    @Override
    public String getType()
    {
        return null;
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return newHashSet("applinks-application-type");
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element element)
    {
        RemoteAppApplicationType applicationType = createApplicationType(applicationTypeClassLoader, element);
        return new ApplicationTypeModule(applicationType,
                createApplicationTypeDescriptor(applicationTypeClassLoader, ctx, applicationType, element),
                mutatingApplicationLinkService, permissionManager, ctx.getAccessLevel());

    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    private RemoteAppApplicationType createApplicationType(ApplicationTypeClassLoader applicationTypeClassLoader, Element element)
    {
        try
        {
            String key = getRequiredAttribute(element, "key");
            Class<? extends RemoteAppApplicationType> applicationTypeClass = applicationTypeClassLoader.getApplicationType(
                    key);
            URI icon = getOptionalUriAttribute(element, "icon-url");
            String label = getRequiredAttribute(element, "name");
            TypeId appId = new TypeId(key);
            URI displayUrl = getRequiredUriAttribute(element, "display-url");
            URI rpcUrl = displayUrl;
            ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                                                                   .displayUrl(displayUrl)
                                                                   .rpcUrl(rpcUrl)
                                                                   .isPrimary(true)
                                                                   .name(label)
                                                                   .build();
            return applicationTypeClass.getConstructor(TypeId.class, String.class, URI.class,
                    ApplicationLinkDetails.class).newInstance(appId, label, icon, details);
        }
        catch (NoSuchMethodException e)
        {
            throw new PluginParseException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new PluginParseException(e);
        }
        catch (InstantiationException e)
        {
            throw new PluginParseException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new PluginParseException(e);
        }
    }

    private ModuleDescriptor<ApplicationType> createApplicationTypeDescriptor(ApplicationTypeClassLoader applicationTypeClassLoader,
                                                                              RemoteAppCreationContext ctx,
                                                                              final RemoteAppApplicationType applicationType,
                                                                              Element element
    )
    {
        final Class<? extends RemoteManifestProducer> manifestProducerClass = applicationTypeClassLoader.getManifestProducer(
                applicationType);

        Element desc = element.createCopy();
        desc.elements().clear();
        desc.addAttribute("key", getGeneratedApplicationTypeModuleKey(applicationType.getId().get()));
        desc.addAttribute("class", applicationType.getClass().getName());
        desc.addElement("manifest-producer").addAttribute("class", manifestProducerClass.getName());

        Class<? extends ModuleDescriptor> descClass = ctx.getModuleDescriptorFactory()
                                                         .getModuleDescriptorClass("applinks-application-type");
        try
        {
            ModuleDescriptor descriptor = descClass.getConstructor(ModuleFactory.class).newInstance(new ModuleFactory()
            {
                @Override
                public <T> T createModule(String s, ModuleDescriptor<T> tModuleDescriptor) throws PluginParseException
                {
                    return (T) applicationType;
                }
            });
            descriptor.init(new DelegatePlugin(ctx.getPlugin())
            {
                @Override
                public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException
                {
                    if (clazz.startsWith("generatedManifestProducer"))
                    {
                        return (Class<T>) manifestProducerClass;
                    }
                    else
                    {
                        return super.loadClass(clazz, callingClass);
                    }
                }
            }, desc);
            return descriptor;
        }
        catch (InstantiationException e)
        {
            throw new PluginParseException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new PluginParseException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new PluginParseException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new PluginParseException(e);
        }
    }

    public static String getGeneratedApplicationTypeModuleKey(String key)
    {
        return "applicationType-" + key;
    }

    @Override
    public void validate(Element root, String registrationUrl, String username)
    {
        String displayUrl = root.attributeValue("display-url");
        if (displayUrl == null || !registrationUrl.startsWith(displayUrl))
        {
            throw new PluginParseException("display-url '" + displayUrl + "' must match registration URL");
        }

        for (ApplicationLink link : mutatingApplicationLinkService.getApplicationLinks())
        {
            if (displayUrl.equals(link.getRpcUrl().toString()))
            {
                throw new PluginParseException("The display url '" + displayUrl + "' is already used by app " +
                    "'" + link.getName() + "'");
            }
        }
    }


    private static class DelegatePlugin extends AbstractDelegatingPlugin implements ContainerManagedPlugin
    {

        public DelegatePlugin(Plugin delegate)
        {
            super(delegate);
        }

        @Override
        public ContainerAccessor getContainerAccessor()
        {
            return ((ContainerManagedPlugin) getDelegate()).getContainerAccessor();
        }
    }
}
