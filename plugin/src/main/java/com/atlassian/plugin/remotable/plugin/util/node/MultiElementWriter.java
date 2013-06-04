package com.atlassian.plugin.remotable.plugin.util.node;

import org.dom4j.Element;

/**
 */
public class MultiElementWriter implements ElementSetWriter
{
    private final Iterable<Node> sources;
    private final Element target;
    private final SingleElementWriter previousWriter;


    public MultiElementWriter(Iterable<Node> sources, Element target, SingleElementWriter previousWriter)
    {
        this.sources = sources;
        this.target = target;
        this.previousWriter = previousWriter;
    }

    @Override
    public ElementSetWriter copyText()
    {
        for (Node node : sources)
        {
            new SingleElementWriter(node, target).copyText();
        }
        return this;
    }

    public ElementSetWriter copyDescription()
    {
        for (Node node : sources)
        {
            new SingleElementWriter(node, target).copyDescription();
        }
        return this;
    }

    public ElementSetWriter copy(String propertyName)
    {
        for (Node node : sources)
        {
            new SingleElementWriter(node, target).copy(propertyName);
        }
        return this;
    }

    @Override
    public ElementSetWriter copyIfExists(String propertyName)
    {
        for (Node node : sources)
        {
            new SingleElementWriter(node, target).copyIfExists(propertyName);
        }
        return this;
    }

    @Override
    public SingleElementWriter done()
    {
        return previousWriter;
    }
}
