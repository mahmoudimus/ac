package com.atlassian.labs.remoteapps.host.common.service.http;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import redstone.xmlrpc.XmlRpcParser;

/**
 * Parser that recognizes fault elements
 */
class FaultHandlingXmlRpcParser extends XmlRpcParser
{
    private Object parsedValue;
    private boolean isFaultResponse;
    @Override
    protected void handleParsedValue(Object obj)
    {
        this.parsedValue = obj;
    }

    /**
     * Override the startElement() method inherited from XmlRpcParser. This way, we may set the
     * error flag if we run into a fault-tag.
     *
     * @param See SAX documentation
     */

    public void startElement(
            String uri,
            String name,
            String qualifiedName,
            Attributes attributes)
            throws SAXException
    {
        if (name.equals("fault"))
        {
            isFaultResponse = true;
        }
        else
        {
            super.startElement(uri, name, qualifiedName, attributes);
        }
    }

    public Object getParsedValue()
    {
        return parsedValue;
    }

    public boolean isFaultResponse()
    {
        return isFaultResponse;
    }
}
