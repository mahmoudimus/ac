package com.atlassian.labs.remoteapps.integration.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.labs.remoteapps.loader.StartableForPlugins;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeClassLoader;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteManifestProducer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.base.Function;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/24/12 Time: 7:40 PM To change this template use
 * File | Settings | File Templates.
 */
public class DynamicApplicationTypeModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration descriptorRegistration;
    private final ApplicationTypeClassLoader applicationTypeClassLoader;
    private final StartableForPlugins startableForPlugins;

    private final PermissionManager permissionManager;
    private final MutatingApplicationLinkService applicationLinkService;
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager oAuthLinkManager;

    private static final Logger log = LoggerFactory.getLogger(DynamicApplicationTypeModuleDescriptor.class);

    private URI displayUrl;
    private URI iconUrl;

    public DynamicApplicationTypeModuleDescriptor(
            DynamicDescriptorRegistration descriptorRegistration,
            ApplicationTypeClassLoader applicationTypeClassLoader,
            StartableForPlugins startableForPlugins, PermissionManager permissionManager,
            MutatingApplicationLinkService applicationLinkService,
            ApplicationLinkAccessor applicationLinkAccessor, OAuthLinkManager oAuthLinkManager)
    {
        this.descriptorRegistration = descriptorRegistration;
        this.applicationTypeClassLoader = applicationTypeClassLoader;
        this.startableForPlugins = startableForPlugins;
        this.permissionManager = permissionManager;
        this.applicationLinkService = applicationLinkService;
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.displayUrl = getRequiredUriAttribute(element, "display-url");
        this.iconUrl = URI.create(displayUrl.toString() + getRequiredUriAttribute(element, "icon-url").getPath());
    }

    @Override
    public void enabled()
    {
        for (ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            if (displayUrl.equals(link.getRpcUrl()))
            {
                throw new PluginParseException("The display url '" + displayUrl + "' is already used by app " +
                        "'" + link.getName() + "'");
            }
        }
        super.enabled();
        RemoteAppApplicationType applicationType = createApplicationType(applicationTypeClassLoader);
        registerApplicationTypeDescriptor(applicationType);
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    public static String getGeneratedApplicationTypeModuleKey(String key)
    {
        return "applicationType-" + key;
    }


    private void registerApplicationTypeDescriptor(
            final RemoteAppApplicationType applicationType
    )
    {
        final Class<? extends RemoteManifestProducer> manifestProducerClass = applicationTypeClassLoader.getManifestProducer(
                applicationType);

        final Element desc = DocumentHelper.createElement("applinks-application-type");
        desc.elements().clear();
        desc.addAttribute("key", getGeneratedApplicationTypeModuleKey(applicationType.getId().get()));
        desc.addAttribute("class", applicationType.getClass().getName());
        desc.addElement("manifest-producer").addAttribute("class", manifestProducerClass.getName());

        descriptorRegistration.createDynamicModuleDescriptor(
                "applinks-application-type", applicationType, new Function<ModuleDescriptor, Void>()
        {
            @Override
            public Void apply(ModuleDescriptor descriptor)
            {
                descriptor.init(new DelegatePlugin(plugin)
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
                descriptorRegistration.registerDescriptors(plugin, descriptor);
                startableForPlugins.register(plugin.getKey(), new ApplicationLinkCreator(applicationType));
                return null;
            }
        });
    }

    private RemoteAppApplicationType createApplicationType(ApplicationTypeClassLoader applicationTypeClassLoader)
    {
        try
        {
            String key = plugin.getKey();
            Class<? extends RemoteAppApplicationType> applicationTypeClass = applicationTypeClassLoader.getApplicationType(
                    key);
            URI icon = iconUrl;
            String label = plugin.getName();
            TypeId appId = new TypeId(key);
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

    private static class DelegatePlugin extends AbstractDelegatingPlugin implements ContainerManagedPlugin
    {

        public DelegatePlugin(Plugin delegate)
        {
            super(delegate);
        }

        @Override
        public ContainerAccessor getContainerAccessor()
        {
            if (getDelegate() instanceof ContainerManagedPlugin)
            {
                return ((ContainerManagedPlugin)getDelegate()).getContainerAccessor();
            }
            else
            {
                return new ContainerAccessor()
                {
                    @Override
                    public <T> T createBean(Class<T> clazz)
                    {
                        try
                        {
                            return clazz.newInstance();
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

                    @Override
                    public <T> Collection<T> getBeansOfType(Class<T> interfaceClass)
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }
    }

    private class ApplicationLinkCreator implements Runnable
    {
        private final RemoteAppApplicationType applicationType;

        private ApplicationLinkCreator(RemoteAppApplicationType applicationType)
        {
            this.applicationType = applicationType;
        }

        @Override
        public void run()
        {
            ApplicationLink link = applicationLinkAccessor.getApplicationLink(applicationType);
            final ApplicationId expectedApplicationId = ApplicationIdUtil.generate(
                    applicationType.getDefaultDetails().getRpcUrl());
            if (link == null)
            {
                log.info("Creating an application link for the remote app type " + applicationType.getId());
                link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, applicationType.getDefaultDetails());
            }
            else
            {
                if (!expectedApplicationId.equals(link.getId()))
                {
                    log.debug("Unexpected application id, removing and adding link");
                    applicationLinkService.deleteApplicationLink(link);
                    link = applicationLinkService.addApplicationLink(expectedApplicationId, applicationType, applicationType.getDefaultDetails());
                }
                else
                {
                    log.info("Applink of type {} already exists", applicationType.getId());
                }
            }
            link.putProperty("IS_ACTIVITY_ITEM_PROVIDER", Boolean.FALSE.toString());

            // ensure no permissions by default
            permissionManager.setApiPermissions(applicationType, Collections.<String>emptyList());

            // set up dummy outgoing link so urls are signed properly.  Will be overwritten if oauth module is defined
            oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(),
                    new ServiceProvider(URI.create("http://localhost"), URI.create("http://localhost"), URI.create("http://localhost")));
        }
    }
}
