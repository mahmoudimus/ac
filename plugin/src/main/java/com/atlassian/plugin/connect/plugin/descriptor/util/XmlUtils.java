package com.atlassian.plugin.connect.plugin.descriptor.util;

import com.atlassian.plugin.connect.spi.xmldescriptor.XmlDescriptor;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * XML utility methods need by all parts of Remotable Plugins
 */
@XmlDescriptor
public final class XmlUtils
{
    private static InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

    private static final EntityResolver EMPTY_ENTITY_RESOLVER = new EntityResolver()
    {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
        {
            return EMPTY_INPUT_SOURCE;
        }
    };

    public static SAXReader createSecureSaxReader()
    {
        return createReader(false);
    }

    private static SAXReader createReader(boolean validating)
    {
        XMLReader xmlReader;
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                        false);
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setEntityResolver(EMPTY_ENTITY_RESOLVER);
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException("XML Parser configured incorrectly", e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Unable to configure XML parser", e);
        }
        return new SAXReader(xmlReader, validating);
    }

    public static SAXReader createSecureValidatingSaxReader()
    {
        return createReader(true);
    }
}
