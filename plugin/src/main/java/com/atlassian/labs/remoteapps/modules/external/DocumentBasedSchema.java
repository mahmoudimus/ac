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
    private final String title;
    private final String description;
    private final String path;
    private final String id;
    private final String complexType;
    private final String maxOccurs;
    private final Plugin plugin;
    private final SchemaTransformer schemaTransformer;

    private DocumentBasedSchema(String title, String description, String path, String id,
            String complexType, String maxOccurs, Plugin plugin,
            SchemaTransformer schemaTransformer)
    {
        this.title = title;
        this.description = description;
        this.path = path;
        this.id = id;
        this.complexType = complexType;
        this.maxOccurs = maxOccurs;
        this.plugin = plugin;
        this.schemaTransformer = schemaTransformer;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getId()
    {
        return id;
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
        private String title;
        private String description;
        private String path;
        private String id;
        private String complexType;
        private String maxOccurs = "unbounded";
        private Plugin plugin;
        private SchemaTransformer schemaTransformer = SchemaTransformer.IDENTITY;

        public DynamicSchemaBuilder()
        {
        }

        public DynamicSchemaBuilder(String id)
        {
            this.id = id + ".xsd";
            this.path = "/xsd/" + this.id;
            this.complexType = dashesToCamelCase(id) + "Type";
            this.title = dashesToTitle(id);
            this.description = "A " + title + " module";
        }

        public DynamicSchemaBuilder setTitle(String title)
        {
            this.title = title;
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

        public DynamicSchemaBuilder setId(String id)
        {
            this.id = id;
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
            checkNotNull(id);
            checkNotNull(title);
            checkNotNull(description);
            checkNotNull(complexType);
            checkNotNull(plugin);
            return new DocumentBasedSchema(title, description, path, id, complexType, maxOccurs, plugin,
                    schemaTransformer);
        }
    }

}
