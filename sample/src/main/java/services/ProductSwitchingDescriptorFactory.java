package services;

import com.atlassian.labs.remoteapps.kit.servlet.RemoteAppDescriptorFactory;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 * Switch descriptors for different apps
 */
@Component
public class ProductSwitchingDescriptorFactory implements RemoteAppDescriptorFactory
{
    @Override
    public Document create()
    {
        try
        {
            return createSecureSaxReader().read(getClass().getResource(
                    "/atlassian-remote-app-" + System.getProperty("product", "refapp") + ".xml"));
        }
        catch (DocumentException e)
        {
            throw new PluginParseException(e);
        }
    }
}
