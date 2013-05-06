package com.atlassian.plugin.remotable.plugin.util.node;

import org.dom4j.Element;

/**
 */
public class SingleElementWriter implements ElementWriter<SingleElementWriter>
{
    private final Node source;
    private final Element target;

    public SingleElementWriter(Node source, Element target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public SingleElementWriter copyText()
    {
        target.addText(source.asString(""));
        return this;
    }

    @Override
    public SingleElementWriter copyDescription()
    {
        Node node = source.get("description");
        if (node.exists())
        {
            Element desc = target.addElement("description");
            new SingleElementWriter(node, desc)
                    .copyIfExists("key")
                    .copyText();
        }
        return this;
    }

    @Override
    public SingleElementWriter copy(String propertyName)
    {
        target.addAttribute(propertyName, source.get(propertyName).asString());
        return this;
    }

    @Override
    public SingleElementWriter copyIfExists(String propertyName)
    {
        Node node = source.get(propertyName);
        if (node.exists())
        {
            target.addAttribute(propertyName, node.asString());
        }
        return this;
    }

    public ElementSetWriter copyNodes(String propertyName)
    {
        return new MultiElementWriter(source.getChildren(propertyName), target, this);
    }
}
