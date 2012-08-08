package com.atlassian.labs.remoteapps.integration.plugins;

import org.dom4j.Document;

/**
 * Transforms a loaded schema
 */
public interface SchemaTransformer
{
    SchemaTransformer IDENTITY = new SchemaTransformer()
    {
        @Override
        public Document transform(Document document)
        {
            return document;
        }
    };

    Document transform(Document document);
}
