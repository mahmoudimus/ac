package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.labs.remoteapps.loader.AggregateModuleDescriptorFactory;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static java.util.Collections.emptyMap;
import static org.objectweb.asm.Opcodes.*;

/**
 * Creates applink entity types
 */
@Component
public class EntityTypeModuleGenerator implements WaitableRemoteModuleGenerator
{
    private final Plugin plugin;
    private final AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory;

    @Autowired
    public EntityTypeModuleGenerator(PluginRetrievalService pluginRetrievalService,
            AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory)
    {
        this.aggregateModuleDescriptorFactory = aggregateModuleDescriptorFactory;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "entity-type";
    }

    @Override
    public String getName()
    {
        return "Entity Type";
    }

    @Override
    public String getDescription()
    {
        return "An application links entity type used for storing the relationship with a local " +
                "application entity like" +
                "            a JIRA project or Confluence space with a similar entity in the Remote App";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "entity.xsd",
                "/xsd/entity.xsd",
                "EntityTypeType");
    }

    @Override
    public void waitToLoad(Element element)
    {
        aggregateModuleDescriptorFactory.waitForRequiredDescriptors("applinks-entity-type");
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
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
        };
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    private RemoteAppEntityType createEntityType(AppTypesClassLoader appTypesClassLoader, NonAppLinksApplicationType type, Element element)
    {
        try
        {
            String key = getRequiredAttribute(element, "key");
            Class<? extends RemoteAppEntityType> entityTypeClass = appTypesClassLoader.generateEntityType(type.getId().get(), key);
            URI icon = getOptionalUriAttribute(element, "icon-url");
            String label = getRequiredAttribute(element, "name");
            TypeId entityId = new TypeId(type.getId().get() + "." + key);
            String pluralizedI18nKey = getRequiredAttribute(element, "pluralized-name");
            return entityTypeClass.getConstructor(TypeId.class, Class.class, String.class, String.class, URI.class)
                                .newInstance(entityId, type.getClass(), label, pluralizedI18nKey, icon);
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


    private ModuleDescriptor<EntityType> createEntityTypeDescriptor(RemoteAppCreationContext ctx, final RemoteAppEntityType entityType, Element element)
    {
        Element desc = element.createCopy();
        String key = getRequiredAttribute(element, "key");
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
