package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.IOException;

public interface ConnectSchemaLocator
{

    String getSchemaForCurrentProduct() throws IOException;

    String getSchema(String schemaPrefix) throws IOException;

    String getPrettySchema(String schemaPrefix) throws IOException;
}
