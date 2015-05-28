package com.atlassian.plugin.connect.core.descriptor;

import java.net.URL;

import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.schema.spi.Schema;

import org.dom4j.Document;

/**
 * Provides information to allow a descriptor to be validated
 */
public interface DescriptorValidatorProvider
{
    public static final String ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH = "/schema/atlassian-plugin-remotable";
    
    String getSchemaNamespace();

    String getRootElementName();

    Iterable<Schema> getModuleSchemas();

    void performSecondaryValidations(Document document) throws InstallationFailedException;

    URL getSchemaUrl();
}
