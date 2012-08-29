package com.atlassian.labs.remoteapps.host.common.descriptor;

import org.dom4j.Document;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.*;

public final class DescriptorUtils
{
    private DescriptorUtils()
    {
    }

    static boolean isAtlassianPluginDescriptor(Document descriptor)
    {
        return isAtlassianPluginDescriptor(descriptor.getRootElement());
    }

    static boolean isRemoteAppDescriptor(Document descriptor)
    {
        return isRemoteAppDescriptor(descriptor.getRootElement());
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
        if (isAtlassianPluginDescriptor(root))
        {
            return getDisplayUrlAttribute(getRemotePluginContainerElement(root));
        }
        else if (isRemoteAppDescriptor(root))
        {
            return getDisplayUrlAttribute(root);
        }
        else
        {
            return null;
        }
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

    private static String getOAuthPublicKey(Element element)
    {
        return text(element(element, "oauth", false), "public-key", false);
    }

    private static boolean isAtlassianPluginDescriptor(Element root)
    {
        checkState(root.isRootElement());
        return root.getName().equals("atlassian-plugin") && root.attribute("plugins-version") != null;
    }

    private static boolean isRemoteAppDescriptor(Element root)
    {
        checkState(root.isRootElement());
        return root.getName().equals("remote-app");
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
        return target != null ? target.getTextTrim() : null;
    }
}
