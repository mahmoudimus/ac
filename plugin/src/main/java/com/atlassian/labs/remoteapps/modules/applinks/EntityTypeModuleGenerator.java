package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
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
import static java.util.Collections.emptySet;
import static org.objectweb.asm.Opcodes.*;

/**
 * Creates applink entity types
 */
@Component
public class EntityTypeModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public EntityTypeModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
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
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element entity)
    {
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
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
}
