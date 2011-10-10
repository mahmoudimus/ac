package com.atlassian.labs.remoteapps.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.BundleUtil.findBundleForPlugin;
import static com.google.common.collect.Sets.newHashSet;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public class RemoteAppModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final BundleContext bundleContext;
    private final PluginEventManager pluginEventManager;
    private final Set<ServiceRegistration> registrations;

    private ServiceTracker serviceTracker;
    private AppTypesClassLoader appTypesClassLoader;
    private Element originalElement;

    public RemoteAppModuleDescriptor(BundleContext bundleContext, PluginEventManager pluginEventManager)
    {
        super(new LegacyModuleFactory());
        this.bundleContext = bundleContext;
        this.pluginEventManager = pluginEventManager;
        registrations = newHashSet();
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.originalElement = element;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        if (appTypesClassLoader == null)
        {
            this.appTypesClassLoader = new AppTypesClassLoader();
            // generate and register new services
            Bundle targetBundle = findBundleForPlugin(bundleContext, getPluginKey());
            final BundleContext targetBundleContext = targetBundle.getBundleContext();
//            AsyncOsgiExecutor executor = new AsyncOsgiExecutor(getPlugin(), targetBundleContext)
//                    .waitForService(ModuleDescriptorFactory.class, new AsyncOsgiExecutor.ServiceCondition() {
//                        public boolean evaluate(ModuleDescriptorFactory factory) {
//                            return factory.hasModuleDescriptor("applinks-application-type");
//                        }
//                    })
//                    .waitForService(MutatingApplicationLinkService.class)
//                    .waitFor("")
            this.serviceTracker = new ServiceTracker(targetBundleContext, ModuleDescriptorFactory.class.getName(), new ServiceTrackerCustomizer()
            {
                private volatile ModuleDescriptorFactory appTypeFactory;
                private volatile ModuleDescriptorFactory entityTypeFactory;
                private volatile ApplinkCreator applinkCreator;

                @Override
                public Object addingService(ServiceReference reference)
                {
                    Object svc = targetBundleContext.getService(reference);
                    ModuleDescriptorFactory factory = (ModuleDescriptorFactory) svc;
                    if (factory.hasModuleDescriptor("applinks-application-type"))
                    {
                        appTypeFactory = factory;
                        registerDescriptors();
                        return svc;
                    }
                    else if (factory.hasModuleDescriptor("applinks-entity-type"))
                    {
                        entityTypeFactory = factory;
                        registerDescriptors();
                        return svc;
                    }
                    return null;
                }

                private void registerDescriptors()
                {
                    if (appTypeFactory != null && entityTypeFactory != null)
                    {

                        RemoteAppApplicationType applicationType = createApplicationType(originalElement);
                        Set<ModuleDescriptor<?>> descriptors = newHashSet();
                        descriptors.add(createApplicationTypeDescriptor(appTypeFactory, applicationType, originalElement));

                        for (Element entity : ((Iterable<Element>)originalElement.elements("entity-type")))
                        {
                            RemoteAppEntityType entityType = createEntityType(applicationType, entity);
                            descriptors.add(createEntityTypeDescriptor(entityTypeFactory, entityType, entity));
                        }
                        for (ModuleDescriptor descriptor : descriptors)
                        {
                            registrations.add(targetBundleContext.registerService(ModuleDescriptor.class.getName(), descriptor, null));
                        }

                        applinkCreator = new ApplinkCreator(getPlugin(), targetBundleContext, pluginEventManager, applicationType);
                    }
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service)
                {
                }

                @Override
                public void removedService(ServiceReference reference, Object service)
                {
                    for (ServiceRegistration reg : registrations)
                    {
                        try
                        {
                            reg.unregister();
                        }
                        catch (IllegalStateException ex)
                        {
                            // no worries, this only means the bundle was already shut down so the services aren't valid anymore
                        }
                    }
                    registrations.clear();
                    applinkCreator.destroy();
                }
            });
            serviceTracker.open();
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        serviceTracker.close();
        serviceTracker = null;
        appTypesClassLoader = null;
    }

    private ModuleDescriptor<EntityType> createEntityTypeDescriptor(ModuleDescriptorFactory factory, final RemoteAppEntityType entityType, Element element)
    {
        Element desc = element.createCopy();
        String key = element.attributeValue("key");
        desc.addAttribute("key", "entityType-" + key);
        desc.addAttribute("class", entityType.getClass().getName());

        Class<? extends ModuleDescriptor> descClass = factory.getModuleDescriptorClass("applinks-entity-type");
        try
        {
            ModuleDescriptor descriptor = descClass.getConstructor(ModuleFactory.class).newInstance(new ModuleFactory()
            {
                @Override
                public <T> T createModule(String s, ModuleDescriptor<T> tModuleDescriptor) throws PluginParseException
                {
                    return (T) entityType;
                }
            });
            descriptor.init(getPlugin(), desc);
            return descriptor;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ModuleDescriptor<ApplicationType> createApplicationTypeDescriptor(ModuleDescriptorFactory factory, final RemoteAppApplicationType applicationType, Element element)
    {
        final Class<? extends RemoteManifestProducer> manifestProducerClass = appTypesClassLoader.generateManifestProducer(applicationType.getId().get(), applicationType.getI18nKey());

        Element desc = element.createCopy();
        desc.addAttribute("key", getGeneratedApplicationTypeModuleKey(getKey()));
        desc.addAttribute("class", applicationType.getClass().getName());
        desc.addElement("manifest-producer").addAttribute("class", manifestProducerClass.getName());

        Class<? extends ModuleDescriptor> descClass = factory.getModuleDescriptorClass("applinks-application-type");
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
            descriptor.init(new DelegatePlugin(getPlugin())
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
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getGeneratedApplicationTypeModuleKey(String key)
    {
        return "applicationType-" + key;
    }

    private RemoteAppApplicationType createApplicationType(Element element)
    {
        try
        {
            Class<? extends RemoteAppApplicationType> applicationTypeClass = appTypesClassLoader.generateApplicationType(getKey());
            URI icon = element.attribute("icon") != null ? new URI(element.attributeValue("icon")) : null;
            String label = getI18nNameKey();
            TypeId appId = new TypeId(getKey());
            String displayUrl = element.attributeValue("display-url");
            String rpcUrl = element.attributeValue("rpc-url");
            rpcUrl = rpcUrl != null ? rpcUrl : displayUrl;
            ApplicationLinkDetails details = ApplicationLinkDetails.builder()
                    .displayUrl(new URI(displayUrl))
                    .rpcUrl(rpcUrl != null ? new URI(rpcUrl) : new URI(displayUrl))
                    .isPrimary(true)
                    .name(label)
                    .build();
            return applicationTypeClass.getConstructor(TypeId.class, String.class, URI.class, ApplicationLinkDetails.class)
                                .newInstance(appId, label, icon, details);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private RemoteAppEntityType createEntityType(RemoteAppApplicationType type, Element element)
    {
        try
        {
            String key = element.attributeValue("key");
            Class<? extends RemoteAppEntityType> entityTypeClass = appTypesClassLoader.generateEntityType(type.getId().get(), key);
            URI icon = element.attribute("icon") != null ? new URI(element.attributeValue("icon")) : null;
            String label = element.attributeValue("i18n-key");
            TypeId entityId = new TypeId(type.getId().get() + "." + key);
            String pluralizedI18nKey = element.attributeValue("pluralized-i18n-key");
            return entityTypeClass.getConstructor(TypeId.class, Class.class, String.class, String.class, URI.class)
                                .newInstance(entityId, type.getClass(), label, pluralizedI18nKey, icon);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    private static class AppTypesClassLoader extends ClassLoader
    {
        public AppTypesClassLoader()
        {
            super(RemoteAppModuleDescriptor.class.getClassLoader());
        }

        public Class<? extends RemoteAppApplicationType> generateApplicationType(String key)
        {
            String genClassName = "generatedApplicationType/"+key+"/_type";
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/applinks/RemoteAppApplicationType", null);

            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/applinks/RemoteAppApplicationType", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 5);
            mv.visitEnd();

            cw.visitEnd();
            byte[] b = cw.toByteArray();
            return (Class<? extends RemoteAppApplicationType>) defineClass(genClassName.replace("/", "."), b, 0, b.length);
        }

        public Class<? extends RemoteAppEntityType> generateEntityType(String appKey, String entityKey)
        {
            String genClassName = "generatedApplicationType/"+appKey+"/" + entityKey;
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/applinks/RemoteAppEntityType", null);

            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class<+Lcom/atlassian/labs/remoteapps/applinks/RemoteAppApplicationType;>;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/applinks/RemoteAppEntityType", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(6, 6);
            mv.visitEnd();

            cw.visitEnd();
            byte[] b = cw.toByteArray();
            return (Class<? extends RemoteAppEntityType>) defineClass(genClassName.replace("/", "."), b, 0, b.length);
        }

        public Class<? extends RemoteManifestProducer> generateManifestProducer(String typeId, String name)
        {
            String genClassName = "generatedManifestProducer/"+typeId+"/_manifest";
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;

            cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/applinks/RemoteManifestProducer", null);

            // constructor that encodes the parameters in the constructor super call
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, "com/atlassian/applinks/spi/application/TypeId");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(typeId);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/applinks/spi/application/TypeId", "<init>", "(Ljava/lang/String;)V");
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/applinks/RemoteManifestProducer", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 1);
            mv.visitEnd();

            cw.visitEnd();
            byte[] b = cw.toByteArray();
            return (Class<? extends RemoteManifestProducer>) defineClass(genClassName.replace("/", "."), b, 0, b.length);
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
            return((ContainerManagedPlugin) getDelegate()).getContainerAccessor();
        }
    }
}
