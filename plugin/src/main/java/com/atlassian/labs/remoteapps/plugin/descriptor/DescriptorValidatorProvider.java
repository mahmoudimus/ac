package com.atlassian.labs.remoteapps.plugin.descriptor;

import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
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
