package com.atlassian.plugin.remotable.spi.schema;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.util.Dom4jUtils;
import com.atlassian.plugin.remotable.spi.util.IdUtils;
import com.atlassian.plugin.Plugin;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Document;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptySet;

/**
 * Schema based on a XML document resource in the plugin
 */
public final class DocumentBasedSchema implements Schema
{
    private final String name;
    private final String description;
    private final String path;
    private final String elementName;
    private final String fileName;
    private final String complexType;
    private final String maxOccurs;
    private final Iterable<String> requiredPermissions;
    private final Iterable<String> optionalPermissions;
    private final Plugin plugin;
    private final SchemaTransformer schemaTransformer;

    private DocumentBasedSchema(String elementName,
                                String name,
                                String description,
                                String path,
                                String fileName,
                                String complexType,
                                String maxOccurs,
                                Iterable<String> requiredPermissions,
                                Iterable<String> optionalPermissions,
                                Plugin plugin,
                                SchemaTransformer schemaTransformer
    )
    {
        this.name = name;
        this.elementName = elementName;
        this.description = description;
        this.path = path;
        this.fileName = fileName;
        this.complexType = complexType;
        this.maxOccurs = maxOccurs;
        this.requiredPermissions = requiredPermissions;
        this.optionalPermissions = optionalPermissions;
        this.plugin = plugin;
        this.schemaTransformer = schemaTransformer;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getFileName()
    {
        return fileName;
    }

    @Override
    public String getElementName()
    {
        return elementName;
    }

    @Override
    public String getComplexType()
    {
        return complexType;
    }

    @Override
    public String getMaxOccurs()
    {
        return maxOccurs;
    }

    @Override
    public Iterable<String> getRequiredPermissions()
    {
        return requiredPermissions;
    }

    @Override
    public Iterable<String> getOptionalPermissions()
    {
        return optionalPermissions;
    }

    @Override
    public Document getDocument()
    {
        final URL sourceUrl = plugin.getResource(path);
        if (sourceUrl == null)
        {
            throw new IllegalStateException("Cannot find schema document " + path);
        }
        Document source = Dom4jUtils.parseDocument(sourceUrl);
        return schemaTransformer.transform(source);
    }

    public static DynamicSchemaBuilder builder()
    {
        return new DynamicSchemaBuilder();
    }

    public static DynamicSchemaBuilder builder(String id)
    {
        return new DynamicSchemaBuilder(id);
    }

    public static class DynamicSchemaBuilder
    {
        private String name;
        private String description;
        private String path;
        private String fileName;
        private String elementName;
        private String complexType;
        private String maxOccurs = "unbounded";

        // default set of permissions for modules is pretty much unrestricted access to backend and front-end code
        private Iterable<String> requiredPermissions = ImmutableSet.of(Permissions.EXECUTE_JAVA, Permissions.GENERATE_ANY_HTML);

        private Iterable<String> optionalPermissions = emptySet();
        private Plugin plugin;
        private SchemaTransformer schemaTransformer = SchemaTransformer.IDENTITY;

        public DynamicSchemaBuilder()
        {
        }

        public DynamicSchemaBuilder(String elementName)
        {
            this.elementName = elementName;
            this.fileName = elementName + ".xsd";
            this.path = "/xsd/" + this.fileName;
            this.complexType = IdUtils.dashesToCamelCase(elementName) + "Type";
            this.name = IdUtils.dashesToTitle(elementName);
            this.description = "A " + name + " module";
        }

        public DynamicSchemaBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public DynamicSchemaBuilder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public DynamicSchemaBuilder setPath(String path)
        {
            this.path = path;
            return this;
        }

        public DynamicSchemaBuilder setFileName(String fileName)
        {
            this.fileName = fileName;
            return this;
        }

        public DynamicSchemaBuilder setElementName(String elementName)
        {
            this.elementName = elementName;
            return this;
        }

        public DynamicSchemaBuilder setRequiredPermissions(Iterable<String> permissions)
        {
            this.requiredPermissions = permissions;
            return this;
        }

        public DynamicSchemaBuilder setOptionalPermissions(Iterable<String> permissions)
        {
            this.optionalPermissions = permissions;
            return this;
        }

        public DynamicSchemaBuilder setComplexType(String complexType)
        {
            this.complexType = complexType;
            return this;
        }

        public DynamicSchemaBuilder setMaxOccurs(String maxOccurs)
        {
            this.maxOccurs = maxOccurs;
            return this;
        }

        public DynamicSchemaBuilder setPlugin(Plugin plugin)
        {
            this.plugin = plugin;
            return this;
        }

        public DynamicSchemaBuilder setTransformer(SchemaTransformer schemaTransformer)
        {
            this.schemaTransformer = schemaTransformer;
            return this;
        }

        public DocumentBasedSchema build()
        {
            checkNotNull(elementName);
            checkNotNull(fileName);
            checkNotNull(name);
            checkNotNull(description);
            checkNotNull(complexType);
            checkNotNull(plugin);
            checkNotNull(requiredPermissions);
            checkNotNull(optionalPermissions);
            return new DocumentBasedSchema(elementName, name, description, path, fileName, complexType, maxOccurs,
                    requiredPermissions, optionalPermissions, plugin,
                    schemaTransformer);
        }
    }

}
