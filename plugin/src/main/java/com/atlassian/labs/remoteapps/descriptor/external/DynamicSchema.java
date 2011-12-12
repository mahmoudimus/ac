package com.atlassian.labs.remoteapps.descriptor.external;

import com.atlassian.plugin.Plugin;
import com.google.common.base.Function;
import org.dom4j.Document;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.transformDocument;
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
    public InputSource getInputSource()
    {
        final URL source = plugin.getResource(path);
        String transformed = transformDocument(source, new Function<Document,Document>()
        {
            @Override
            public Document apply(Document from)
            {
                return transform(from);
            }
        });
        InputSource result = new InputSource(new StringReader(transformed));
        result.setSystemId(source.toString());
        return result;
    }

    protected abstract Document transform(Document from);
}
