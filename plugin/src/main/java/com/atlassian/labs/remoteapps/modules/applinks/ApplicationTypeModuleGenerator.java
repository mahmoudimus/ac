package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
@Component
public class ApplicationTypeModuleGenerator implements RemoteModuleGenerator
{
    private final MutatingApplicationLinkService mutatingApplicationLinkService;

    @Autowired
    public ApplicationTypeModuleGenerator(MutatingApplicationLinkService mutatingApplicationLinkService)
    {
        this.mutatingApplicationLinkService = mutatingApplicationLinkService;
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
    public RemoteModule generate(RemoteAppCreationContext ctx, Element element)
    {
        AppTypesClassLoader appTypesClassLoader = new AppTypesClassLoader();
        RemoteAppApplicationType applicationType = createApplicationType(appTypesClassLoader, element);
        return new ApplicationTypeModule(applicationType, createApplicationTypeDescriptor(appTypesClassLoader, ctx, applicationType, element), mutatingApplicationLinkService);

    }

    private RemoteAppApplicationType createApplicationType(AppTypesClassLoader appTypesClassLoader, Element element)
    {
        try
        {
            String key = element.attributeValue("key");
            Class<? extends RemoteAppApplicationType> applicationTypeClass = appTypesClassLoader.generateApplicationType(key);
            URI icon = element.attribute("icon") != null ? new URI(element.attributeValue("icon")) : null;
            String label = element.attributeValue("i18n-name-key");
            TypeId appId = new TypeId(key);
            String displayUrl = element.attributeValue("display-url");
            String rpcUrl = element.attributeValue("rpc-url");
            rpcUrl = rpcUrl != null ? rpcUrl : displayUrl;
            ApplicationLinkDetails details = ApplicationLinkDetails.builder().displayUrl(new URI(displayUrl)).rpcUrl(rpcUrl != null ? new URI(rpcUrl) : new URI(displayUrl)).isPrimary(true).name(label).build();
            return applicationTypeClass.getConstructor(TypeId.class, String.class, URI.class, ApplicationLinkDetails.class).newInstance(appId, label, icon, details);
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

    private ModuleDescriptor<ApplicationType> createApplicationTypeDescriptor(AppTypesClassLoader appTypesClassLoader, RemoteAppCreationContext ctx, final RemoteAppApplicationType applicationType, Element element)
    {
        final Class<? extends RemoteManifestProducer> manifestProducerClass = appTypesClassLoader.generateManifestProducer(applicationType.getId().get(), applicationType.getI18nKey());

        Element desc = element.createCopy();
        desc.addAttribute("key", getGeneratedApplicationTypeModuleKey(applicationType.getId().get()));
        desc.addAttribute("class", applicationType.getClass().getName());
        desc.addElement("manifest-producer").addAttribute("class", manifestProducerClass.getName());

        Class<? extends ModuleDescriptor> descClass = ctx.getModuleDescriptorFactory().getModuleDescriptorClass("applinks-application-type");
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


    private static class AppTypesClassLoader extends ClassLoader
    {
        public AppTypesClassLoader()
        {
            super(ApplicationTypeModuleGenerator.class.getClassLoader());
        }

        public Class<? extends RemoteAppApplicationType> generateApplicationType(String key)
        {
            String genClassName = "generatedApplicationType/" + key + "/_type";
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppApplicationType", null);

            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppApplicationType", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 5);
            mv.visitEnd();

            cw.visitEnd();
            byte[] b = cw.toByteArray();
            return (Class<? extends RemoteAppApplicationType>) defineClass(genClassName.replace("/", "."), b, 0, b.length);
        }

        public Class<? extends RemoteManifestProducer> generateManifestProducer(String typeId, String name)
        {
            String genClassName = "generatedManifestProducer/" + typeId + "/_manifest";
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;

            cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/modules/applinks/RemoteManifestProducer", null);

            // constructor that encodes the parameters in the constructor super call
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, "com/atlassian/applinks/spi/application/TypeId");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(typeId);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/applinks/spi/application/TypeId", "<init>", "(Ljava/lang/String;)V");
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/modules/applinks/RemoteManifestProducer", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;)V");
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
