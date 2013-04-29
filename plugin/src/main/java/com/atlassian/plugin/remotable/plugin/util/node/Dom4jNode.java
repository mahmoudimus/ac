package com.atlassian.plugin.remotable.plugin.util.node;

import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 */
public class Dom4jNode implements Node
{
    private final Element element;

    public Dom4jNode(Element element)
    {
        this.element = element;
    }

    @Override
    public Node get(String propertyName)
    {
        String val = element.attributeValue(propertyName);
        if (val == null)
        {
            Element child = element.element(propertyName);
            if (child == null)
            {
                return Node.MISSING;
            }
            else
            {
                return new Dom4jNode(child);
            }
        }
        else
        {
            return new ValueNode(val);
        }
    }

    @Override
    public Iterable<Node> getChildren(String nodeName)
    {
        return Iterables.<Object, Node>transform(element.elements(nodeName), new Function<Object, Node>()
        {
            @Override
            public Node apply(Object input)
            {
                return new Dom4jNode((Element)input);
            }
        });
    }

    @Override
    public String asString()
    {
        if (element.getText() != null)
        {
           return element.getTextTrim();
        }
        else
        {
            throw new PluginParseException("String value not found");
        }
    }

    @Override
    public String asString(String defaultValue)
    {
        if (element.getText() != null)
        {
            return element.getTextTrim();
        }
        else
        {
            return defaultValue;
        }
    }

    @Override
    public String toString()
    {
        return asString("null");
    }

    @Override
    public URI asURI()
    {
        return URI.create(asString());
    }

    @Override
    public int asInt()
    {
        return Integer.parseInt(asString());
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public boolean asBoolean(boolean defaultValue)
    {
        return Boolean.parseBoolean(asString(Boolean.toString(defaultValue)));
    }
}
