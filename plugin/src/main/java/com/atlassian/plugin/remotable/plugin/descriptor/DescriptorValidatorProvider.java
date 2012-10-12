package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.schema.spi.Schema;
import org.dom4j.Document;

import java.net.URL;

/**
 * Provides information to allow a descriptor to be validated
 */
public interface DescriptorValidatorProvider
{
    String getSchemaNamespace();

    String getRootElementName();

    Iterable<Schema> getModuleSchemas();

    void performSecondaryValidations(Document document) throws InstallationFailedException;

    URL getSchemaUrl();
}
