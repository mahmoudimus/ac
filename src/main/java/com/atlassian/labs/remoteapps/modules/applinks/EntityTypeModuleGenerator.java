package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 *
 */
public class EntityTypeModuleGenerator implements RemoteModuleGenerator
{
    @Override
    public String getType()
    {
        return "entity-type";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return ImmutableSet.of("applinks-entity-type");
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element entity)
    {
        AppTypesClassLoader appTypesClassLoader = new AppTypesClassLoader();
        RemoteAppEntityType entityType = createEntityType(appTypesClassLoader, ctx.getApplicationType(), entity);
        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(createEntityTypeDescriptor(ctx, entityType, entity));
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }

            @Override
            public void close()
            {
            }
        };
    }

    private RemoteAppEntityType createEntityType(AppTypesClassLoader appTypesClassLoader, NonAppLinksApplicationType type, Element element)
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


    private ModuleDescriptor<EntityType> createEntityTypeDescriptor(RemoteAppCreationContext ctx, final RemoteAppEntityType entityType, Element element)
    {
        Element desc = element.createCopy();
        String key = element.attributeValue("key");
        desc.addAttribute("key", "entityType-" + key);
        desc.addAttribute("class", entityType.getClass().getName());

        Class<? extends ModuleDescriptor> descClass = ctx.getModuleDescriptorFactory().getModuleDescriptorClass("applinks-entity-type");
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
            descriptor.init(ctx.getPlugin(), desc);
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

    private static class AppTypesClassLoader extends ClassLoader
    {
        public AppTypesClassLoader()
        {
            super(EntityTypeModuleGenerator.class.getClassLoader());
        }

        public Class<? extends RemoteAppEntityType> generateEntityType(String appKey, String entityKey)
        {
            String genClassName = "generatedApplicationType/" + appKey + "/" + entityKey;
            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, genClassName, null, "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppEntityType", null);

            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class<+Lcom/atlassian/labs/remoteapps/modules/applinks/RemoteAppApplicationType;>;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppEntityType", "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Ljava/net/URI;)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(6, 6);
            mv.visitEnd();

            cw.visitEnd();
            byte[] b = cw.toByteArray();
            return (Class<? extends RemoteAppEntityType>) defineClass(genClassName.replace("/", "."), b, 0, b.length);
        }
    }
}
