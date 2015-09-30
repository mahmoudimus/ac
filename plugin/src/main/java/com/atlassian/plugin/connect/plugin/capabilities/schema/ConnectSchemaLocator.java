package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.IOException;

public interface ConnectSchemaLocator
{
    String getSchema(String schemaPrefix) throws IOException;

    String getPrettySchema(String schemaPrefix) throws IOException;
    
    String getShallowSchema() throws IOException;
}
