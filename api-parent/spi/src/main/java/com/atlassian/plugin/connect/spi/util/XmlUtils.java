package com.atlassian.plugin.connect.spi.util;

import com.atlassian.security.xml.SecureXmlParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * XML utility methods need by all parts of Remotable Plugins
 */
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
        xmlReader = SecureXmlParserFactory.newXmlReader();
        xmlReader.setEntityResolver(EMPTY_ENTITY_RESOLVER);

        return new SAXReader(xmlReader, validating);
    }

    public static SAXReader createSecureValidatingSaxReader()
    {
        return createReader(true);
    }
}
