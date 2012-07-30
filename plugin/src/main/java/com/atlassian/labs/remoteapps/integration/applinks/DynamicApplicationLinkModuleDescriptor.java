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
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppEntityType;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteManifestProducer;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.base.Function;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.List;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredElementText;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/24/12 Time: 7:40 PM To change this template use
 * File | Settings | File Templates.
 */
public class DynamicApplicationLinkModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration descriptorRegistration;
    private final ApplicationTypeClassLoader applicationTypeClassLoader;
    private final StartableForPlugins startableForPlugins;

    private final MutatingApplicationLinkService applicationLinkService;
    private final ApplicationLinkAccessor applicationLinkAccessor;
    private final OAuthLinkManager oAuthLinkManager;

    private static final Logger log = LoggerFactory.getLogger(DynamicApplicationLinkModuleDescriptor.class);

    private URI displayUrl;
    private URI iconUrl;
    private List<Element> entityElements;
    private Element oauthElement;

    public DynamicApplicationLinkModuleDescriptor(
            DynamicDescriptorRegistration descriptorRegistration,
            ApplicationTypeClassLoader applicationTypeClassLoader,
            StartableForPlugins startableForPlugins,
            MutatingApplicationLinkService applicationLinkService,
            ApplicationLinkAccessor applicationLinkAccessor, OAuthLinkManager oAuthLinkManager)
    {
        this.descriptorRegistration = descriptorRegistration;
        this.applicationTypeClassLoader = applicationTypeClassLoader;
        this.startableForPlugins = startableForPlugins;
        this.applicationLinkService = applicationLinkService;
        this.applicationLinkAccessor = applicationLinkAccessor;
        this.oAuthLinkManager = oAuthLinkManager;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.entityElements = element.elements("entity-type");
        this.oauthElement = element.element("oauth");
        this.displayUrl = getRequiredUriAttribute(element, "display-url");
        if (element.attribute("icon-url") != null)
        {
            this.iconUrl = URI.create(displayUrl.toString() + getRequiredUriAttribute(element, "icon-url").getPath());
        }
    }

    @Override
    public void enabled()
    {
        // this works because links for which the type cannot be found won't be returned.  Only down
        // below do we register the type
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
        ServiceProvider serviceProvider = createOAuthServiceProvider(applicationType, oauthElement);
        registerApplicationTypeDescriptor(applicationType, serviceProvider);
        for (Element entityElement : entityElements)
        {
            RemoteAppEntityType entityType = createEntityType(getPluginKey(), applicationType.getClass(), entityElement);
            registerEntityTypeDescriptor(entityType, entityElement);
        }
        if (oauthElement != null)
        {
            registerOAuth(applicationType, oauthElement);
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        oAuthLinkManager.unassociateConsumer(
                Consumer.
                        key(getPluginKey()).
                        name("Doesn't Matter").
                        signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build());
    }

    private ServiceProvider createOAuthServiceProvider(RemoteAppApplicationType remoteAppApplicationType, Element oauthElement)
    {
        URI baseUrl = remoteAppApplicationType.getDefaultDetails().getDisplayUrl();
        if (oauthElement != null)
        {
            final URI requestTokenUrl = URI.create(baseUrl + getOptionalAttribute(oauthElement, "request-token-url", "/request-token"));
            final URI accessTokenUrl = URI.create(baseUrl + getOptionalAttribute(oauthElement, "access-token-url", "/access-token"));
            final URI authorizeUrl = URI.create(baseUrl + getOptionalAttribute(oauthElement, "authorize-url", "/authorize"));
            return new ServiceProvider(requestTokenUrl, accessTokenUrl, authorizeUrl);
        }
        else
        {
            // set up the link with a dummy so that outgoing links get signed even if no oauth element
            // is defined
            URI dummyUri = URI.create("http://localhost");
            return new ServiceProvider(dummyUri, dummyUri, dummyUri);
        }
    }

    private void registerOAuth(RemoteAppApplicationType applicationType, Element oauthElement)
    {
        final PluginInformation pluginInfo = getPlugin().getPluginInformation();
        final String name = getPlugin().getName();
        final String description = pluginInfo.getDescription();
        URI baseUrl = applicationType.getDefaultDetails().getDisplayUrl();
        final URI callback = URI.create(baseUrl + getOptionalAttribute(oauthElement, "callback", "/callback"));
        final PublicKey publicKey = getPublicKey(getRequiredElementText(oauthElement, "public-key"));

        Consumer consumer = Consumer.key(getPluginKey()).name(name != null ? name : getPluginKey()).publicKey(publicKey).description(description).callback(
                        callback).build();

        ApplicationLink link = applicationLinkAccessor.getApplicationLink(getPluginKey());

        oAuthLinkManager.associateConsumerWithLink(link, consumer);

        // provider is already configured as part of the applink creation
    }

    protected final PublicKey getPublicKey(String publicKeyText)
    {
        PublicKey publicKey;
        try
        {
            if (publicKeyText.startsWith("-----BEGIN CERTIFICATE-----"))
            {
                publicKey = RSAKeys.fromEncodedCertificateToPublicKey(publicKeyText);
            }
            else
            {
                publicKey = RSAKeys.fromPemEncodingToPublicKey(publicKeyText);
            }
        }
        catch (GeneralSecurityException e)
        {
            throw new PluginParseException("Invalid public key", e);
        }
        return publicKey;
    }

    private RemoteAppEntityType createEntityType(String appKey,
            Class<? extends RemoteAppApplicationType> applicationTypeClass, Element element)
    {
        try
        {
            String key = getRequiredAttribute(element, "key");
            Class<? extends RemoteAppEntityType> entityTypeClass = applicationTypeClassLoader.generateEntityType(appKey, key);
            URI icon = getOptionalUriAttribute(element, "icon-url");
            String label = getRequiredAttribute(element, "name");
            TypeId entityId = new TypeId(appKey + "." + key);
            String pluralizedI18nKey = getRequiredAttribute(element, "pluralized-name");
            return entityTypeClass.getConstructor(TypeId.class, Class.class, String.class, String.class, URI.class)
                    .newInstance(entityId, applicationTypeClass, label, pluralizedI18nKey, icon);
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


    private void registerEntityTypeDescriptor(final RemoteAppEntityType entityType, Element element)
    {
        final Element desc = element.createCopy();
        String key = getRequiredAttribute(element, "key");
        desc.addAttribute("key", "entityType-" + key);
        desc.addAttribute("class", entityType.getClass().getName());

        descriptorRegistration.createDynamicModuleDescriptor(
                "applinks-entity-type", entityType, new Function<ModuleDescriptor, Void>()
        {
            @Override
            public Void apply(ModuleDescriptor descriptor)
            {
                descriptor.init(getPlugin(),  desc);
                descriptorRegistration.registerDescriptors(plugin, descriptor);
                return null;
            }
        });
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
            final RemoteAppApplicationType applicationType, final ServiceProvider serviceProvider
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
                startableForPlugins.register(plugin.getKey(), new ApplicationLinkCreator(applicationType,
                        serviceProvider));
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

    private class ApplicationLinkCreator implements Runnable
    {
        private final RemoteAppApplicationType applicationType;
        private final ServiceProvider serviceProvider;

        private ApplicationLinkCreator(RemoteAppApplicationType applicationType,
                ServiceProvider serviceProvider)
        {
            this.applicationType = applicationType;
            this.serviceProvider = serviceProvider;
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

            // set up dummy outgoing link so urls are signed properly.  Will be overwritten if oauth module is defined
            oAuthLinkManager.associateProviderWithLink(link, applicationType.getId().get(), serviceProvider);
        }
    }
}
