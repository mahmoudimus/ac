package com.atlassian.plugin.remotable.plugin.util.node;

import com.atlassian.plugin.PluginParseException;

import java.net.URI;

import static java.util.Collections.emptyList;

/**
 */
public interface Node
{

    Node get(String propertyName);
    Iterable<Node> getChildren(String nodeName);

    String asString();
    String asString(String defaultValue);
    URI asURI();
    int asInt();
    boolean exists();
    boolean asBoolean(boolean defaultValue);

    Node MISSING = new Node()
    {
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
            throw new PluginParseException("Value not found");
        }

        @Override
        public String asString(String defaultValue)
        {
            return defaultValue;
        }

        @Override
        public URI asURI()
        {
            throw new PluginParseException("URI not found");
        }

        @Override
        public int asInt()
        {
            throw new PluginParseException("Integer not found");
        }

        @Override
        public boolean exists()
        {
            return false;
        }

        @Override
        public boolean asBoolean(boolean defaultValue)
        {
            return defaultValue;
        }
    };

}
