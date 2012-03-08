package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.plugin.Plugin;
import org.dom4j.Document;

import java.net.URL;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static org.apache.commons.lang.Validate.notNull;

/**
 *
 */
public abstract class DynamicSchema implements Schema
{
    private final String path;
    private final String id;
    private final String complexType;
    private final String maxOccurs;
    private final Plugin plugin;

    protected DynamicSchema(Plugin plugin, String id, String path, String complexType, String maxOccurs)
    {
        this.plugin = plugin;
        notNull(id);
        notNull(path);
        notNull(complexType);
        this.id = id;
        this.path = path;
        this.complexType = complexType;
        this.maxOccurs = maxOccurs;
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
        Document source = parseDocument(sourceUrl);
        return transform(source);
    }

    protected abstract Document transform(Document from);
}
