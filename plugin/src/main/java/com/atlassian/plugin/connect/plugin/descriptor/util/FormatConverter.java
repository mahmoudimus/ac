package com.atlassian.plugin.connect.plugin.descriptor.util;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;
import java.util.Set;

@XmlDescriptor
@Component
public final class FormatConverter
{
    private final Set<String> appProperties = ImmutableSet.of(
            "key", "name", "version", "display-url", "icon-url");

    private static final Logger log = LoggerFactory.getLogger(FormatConverter.class);

    public Document toDocument(String id, String contentType, String text)
    {
        if ("text/yaml".equals(contentType) || "application/json".equals(contentType) ||
            (id != null && (id.endsWith(".yaml") || id.endsWith(".json"))))
        {
            throw new InvalidDescriptorException("YAML / JSON descriptors are no longer supported!");
        }
        else
        {
            return loadAsXml(id, text);
        }
    }

    public Document readFileToDoc(File file)
    {
        SAXReader reader = XmlUtils.createSecureSaxReader();
        try
        {
            InputSource source = new InputSource(Files.newReader(file, Charsets.UTF_8));
            source.setEncoding("UTF-8");
            Document document = reader.read(file);

            XmlDescriptorExploder.notifyAndExplode(null == document ? null : document.getRootElement().attributeValue("key"));

            document.accept(new NamespaceCleaner());
            return document;
        }
        catch (Exception e)
        {
            throw new InvalidDescriptorException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
    }

    private Document loadAsXml(String id, String text)
    {
        SAXReader reader = XmlUtils.createSecureSaxReader();
        try
        {
            InputSource source = new InputSource(new StringReader(text));
            source.setSystemId(id);
            source.setEncoding("UTF-8");
            Document document = reader.read(source);

            XmlDescriptorExploder.notifyAndExplode(null == document ? null : document.getRootElement().attributeValue("key"));

            document.accept(new NamespaceCleaner());
            return document;
        }
        catch (DocumentException e)
        {
            throw new InvalidDescriptorException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
    }

    private static class NamespaceCleaner extends VisitorSupport
    {
        public void visit(Document document)
        {
            ((DefaultElement) document.getRootElement())
                    .setNamespace(Namespace.NO_NAMESPACE);
            document.getRootElement().additionalNamespaces().clear();
        }

        public void visit(Namespace namespace)
        {
            namespace.detach();
        }

        public void visit(Attribute node)
        {
            if (node.toString().contains("xmlns")
                    || node.toString().contains("xsi:"))
            {
                node.detach();
            }
        }

        public void visit(Element node)
        {
            if (node instanceof DefaultElement)
            {
                ((DefaultElement) node).setNamespace(Namespace.NO_NAMESPACE);
            }
        }

    }
}
