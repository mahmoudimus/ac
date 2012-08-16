package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.labs.remoteapps.integration.plugins.SchemaTransformer;
import com.atlassian.plugin.Plugin;
import org.dom4j.Document;

import java.net.URL;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static com.atlassian.labs.remoteapps.util.IdUtils.dashesToCamelCase;
import static com.atlassian.labs.remoteapps.util.IdUtils.dashesToTitle;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Schema based on a XML document resource in the plugin
 */
public class DocumentBasedSchema implements Schema
{
    private final String name;
    private final String description;
    private final String path;
    private final String elementName;
    private final String fileName;
    private final String complexType;
    private final String maxOccurs;
    private final Plugin plugin;
    private final SchemaTransformer schemaTransformer;

    private DocumentBasedSchema(String elementName, String name, String description, String path, String fileName,
            String complexType, String maxOccurs, Plugin plugin,
            SchemaTransformer schemaTransformer)
    {
        this.name = name;
        this.elementName = elementName;
        this.description = description;
        this.path = path;
        this.fileName = fileName;
        this.complexType = complexType;
        this.maxOccurs = maxOccurs;
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
    public Document getDocument()
    {
        final URL sourceUrl = plugin.getResource(path);
        if (sourceUrl == null)
        {
            throw new IllegalStateException("Cannot find schema document " + path);
        }
        Document source = parseDocument(sourceUrl);
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
            this.complexType = dashesToCamelCase(elementName) + "Type";
            this.name = dashesToTitle(elementName);
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
            return new DocumentBasedSchema(elementName, name, description, path, fileName, complexType, maxOccurs, plugin,
                    schemaTransformer);
        }
    }

}
