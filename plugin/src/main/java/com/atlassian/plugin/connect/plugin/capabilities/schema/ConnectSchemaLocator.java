package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.IOException;

import com.atlassian.plugin.connect.modules.util.ProductFilter;

public interface ConnectSchemaLocator
{

    String getPrettySchemaForCurrentProduct() throws IOException;

    String getPrettySchema(ProductFilter productFilter) throws IOException;

    String getSchemaForCurrentProduct() throws IOException;

    String getSchema(ProductFilter productFilter) throws IOException;
}
