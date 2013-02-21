package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.schema.spi.Schema;
import org.dom4j.Document;

import java.net.URL;

/**
 * Provides information to allow a descriptor to be validated
 */
public interface DescriptorValidatorProvider
{
    String getSchemaNamespace(InstallationMode installationMode);

    String getRootElementName();

    Iterable<Schema> getModuleSchemas(InstallationMode mode);

    void performSecondaryValidations(Document document) throws InstallationFailedException;

    URL getSchemaUrl();
}
