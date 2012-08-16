package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.modules.external.Schema;

import java.net.URL;

/**
 * Provides information to allow a descriptor to be validated
 */
public interface DescriptorValidatorProvider
{
    String getSchemaNamespace();

    String getRootElementName();

    Iterable<Schema> getModuleSchemas();

    URL getSchemaUrl();
}
