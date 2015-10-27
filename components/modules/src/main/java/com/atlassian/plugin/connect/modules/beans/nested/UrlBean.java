package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#URL_EXAMPLE}
 * @schemaTitle URL
 * @since 1.0
 */
@SchemaDefinition("url")
public class UrlBean
{
    @StringSchemaAttributes(format = "uri-template")
    @Required
    private String url;

    public UrlBean(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public boolean hasUrl()
    {
        return null != url;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }

}
