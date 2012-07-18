package services;

import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Component;

import java.net.URL;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 * Switch descriptors for different apps
 */
@Component
public class ProductSwitchingRemoteAppDescriptorAccessor implements RemoteAppDescriptorAccessor
{
    private Document create()
    {
        try
        {
            return createSecureSaxReader().read(getDescriptorUrl());
        }
        catch (DocumentException e)
        {
            throw new PluginParseException(e);
        }
    }

    @Override
    public Document getDescriptor()
    {
        return create();
    }

    @Override
    public String getKey()
    {
        return create().getRootElement().attributeValue("key");
    }

    @Override
    public URL getDescriptorUrl()
    {
        return getClass().getResource(
                "/atlassian-remote-app-" + System.getProperty("product", "refapp") + ".xml");
    }
}
