package com.atlassian.plugin.remotable.plugin.util.node;

import java.net.URI;

import static java.util.Collections.emptyList;

/**
 */
public class ValueNode implements Node
{
    private final Object value;

    public ValueNode(Object value)
    {
        this.value = value;
    }

    @Override
    public Node get(String propertyName)
    {
        return MISSING;
    }

    @Override
    public Iterable<Node> getChildren(String nodeName)
    {
        return emptyList();
    }

    @Override
    public String asString()
    {
        return value.toString();
    }

    @Override
    public String asString(String defaultValue)
    {
        return value.toString();
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

    @Override
    public String toString()
    {
        return asString("null");
    }
}
