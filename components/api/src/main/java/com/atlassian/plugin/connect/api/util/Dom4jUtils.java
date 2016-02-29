package com.atlassian.plugin.connect.api.util;

import com.atlassian.security.xml.SecureXmlParserFactory;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.servlet.ServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public final class Dom4jUtils {

    private static InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

    private static final EntityResolver EMPTY_ENTITY_RESOLVER = (publicId, systemId) -> EMPTY_INPUT_SOURCE;

    public static String printNode(Node document) {
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(document);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to write node", e);
        }
        return writer.toString();
    }

    public static Document readDocument(ServletRequest request) {
        try {
            return readDocument(request.getInputStream());
        } catch (IOException e) {
            // ignore
            return null;
        }
    }

    public static Document readDocument(InputStream in) {
        SAXReader build = createSecureSaxReader();
        try {
            return build.read(in);
        } catch (DocumentException e) {
            // don't care why
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private static SAXReader createSecureSaxReader() {
        return createReader(false);
    }

    private static SAXReader createReader(boolean validating) {
        XMLReader xmlReader;
        xmlReader = SecureXmlParserFactory.newXmlReader();
        xmlReader.setEntityResolver(EMPTY_ENTITY_RESOLVER);

        return new SAXReader(xmlReader, validating);
    }
}
