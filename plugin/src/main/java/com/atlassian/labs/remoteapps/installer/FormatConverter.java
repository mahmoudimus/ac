package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.api.XmlUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.printDocument;

@Component
public class FormatConverter
{
    private final ModuleGeneratorManager moduleGeneratorManager;
    private static final Logger log = LoggerFactory.getLogger(FormatConverter.class);

    @Autowired
    public FormatConverter(ModuleGeneratorManager moduleGeneratorManager)
    {
        this.moduleGeneratorManager = moduleGeneratorManager;
    }

    public Document toDocument(String id, String contentType, String text)
    {
        if ("text/yaml".equals(contentType) || "application/json".equals(contentType) ||
            (id != null && (id.endsWith(".yaml") || id.endsWith(".json"))))
        {
            return loadAsYaml(text);
        }
        else
        {
            return loadAsXml(id, text);
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
            document.accept(new NamespaceCleaner());
            return document;
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
    }

    private Document loadAsYaml(String descriptorXml)
    {
        Yaml yaml = new Yaml();
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("remote-app");
        Set<String> moduleKeys = moduleGeneratorManager.getModuleGeneratorKeys();
        Map<String,Object> data = yaml.loadAs(descriptorXml, Map.class);
        for (Map.Entry<String,Object> entry : data.entrySet())
        {
            if (entry.getKey().equals("modules"))
            {
                for (Map<String,Object> module : (Collection<Map<String,Object>>)entry.getValue())
                {
                    String key = getObjectSingleKey(module);
                    processObject(key, module.get(key), root);
                }
            }
            else if (moduleKeys.contains(entry.getKey()) || entry.getKey().equals("description"))
            {
                processObject(entry.getKey(), entry.getValue(), root);
            }
            else
            {
                root.addAttribute(entry.getKey(), entry.getValue().toString());
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("Transformed YAML to\n" + printDocument(doc));
        }
        return doc;
    }

    private String getObjectSingleKey(Map<String, Object> module)
    {
        return module.size() == 1 ? module.keySet().iterator().next() : null;
    }

    private void processObject(String name, Object object, Element parent)
    {
        if (object instanceof Map)
        {
            Element self = parent.addElement(name);
            for (Map.Entry<String,Object> entry : ((Map<String,Object>)object).entrySet())
            {
                processObject(entry.getKey(), entry.getValue(), self);
            }
        }
        else if (object instanceof List)
        {
            if (name.equals("permissions"))
            {
                Element perms = parent.addElement("permissions");
                for (Map entry : (List<Map>)object)
                {
                    processObject("permission", entry.values().iterator().next(), perms);
                }
            }
            else
            {
                for (Object entry : (List)object)
                {
                    processObject(name, entry, parent);
                }
            }
        }
        else if (name.equals("description") || name.equals("public-key"))
        {
            parent.addElement(name).setText(object.toString());
        }
        else
        {
            parent.addAttribute(name, object.toString());
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
