package com.atlassian.plugin.remotable.host.common.descriptor;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;

import static com.google.common.base.Preconditions.*;

public final class DescriptorUtils
{
    private DescriptorUtils()
    {
    }

    public static String getDisplayUrl(Document descriptor)
    {
        return getDisplayUrl(descriptor.getRootElement());
    }

    public static String getOAuthPublicKey(Document descriptor)
    {
        final String value;
        final Element root = descriptor.getRootElement();
        if (isAtlassianPluginDescriptor(descriptor.getRootElement()))
        {
            value = getOAuthPublicKey(getRemotePluginContainerElement(root));
        }
        else
        {
            value = getOAuthPublicKey(root);
        }
        return value;
    }

    private static String getDisplayUrl(Element root)
    {
        checkState(root.isRootElement());
        return getDisplayUrlAttribute(getRemotePluginContainerElement(root));
    }

    private static String getDisplayUrlAttribute(Element element)
    {
        return element.attributeValue("display-url");
    }

    public static Element getRemotePluginContainerElement(Element root)
    {
        checkState(root.isRootElement());
        return element(root, "remote-plugin-container");
    }

    public static Document addDisplayUrl(Element element, String displayUrl)
    {
        return element.addAttribute("display-url", displayUrl).getDocument();
    }

    public static Document addRegistrationWebHook(Element root)
    {
        for (Element webHook : (List<Element>) root.elements("webhook"))
        {
            if ("remote_plugin_installed".equals(webHook.attributeValue("event")))
            {
                return root.getDocument();
            }
        }

        // not found
        root.addElement("webhook").addAttribute("event", "remote_plugin_installed")
                .addAttribute("url", "/")
                .addAttribute("key", "_registration");
        return root.getDocument();
    }

    private static String getOAuthPublicKey(Element element)
    {
        return text(element(element, "oauth", false), "public-key", false);
    }

    private static boolean isAtlassianPluginDescriptor(Element root)
    {
        checkState(root.isRootElement());
        return root.getName().equals("atlassian-plugin") && root.attribute("plugins-version") != null;
    }

    private static Element element(Element parent, String name)
    {
        return element(parent, name, true);
    }

    private static Element element(Element parent, String name, boolean required)
    {
        Element child = parent != null ? parent.element(name) : null;
        if (required && child == null)
        {
            String pname = parent != null ? parent.getName() : null;
            throw new IllegalStateException("Required element '" + name + "' to be present on '" + pname + "' element");
        }
        return child;
    }

    private static String text(Element parent, String name, boolean required)
    {
        Element target = element(parent, name, required);
        return target != null ? removeSpacesOnEnds(target.getText()) : null;
    }

    private static String removeSpacesOnEnds(String text)
    {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n|\r\n|\r"))
        {
            sb.append(line.trim()).append("\n");
        }
        return sb.toString();
    }
}
