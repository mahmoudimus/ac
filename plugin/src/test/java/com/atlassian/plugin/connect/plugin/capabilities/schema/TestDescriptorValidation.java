package com.atlassian.plugin.connect.plugin.capabilities.schema;

import com.atlassian.plugin.spring.scanner.ProductFilter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDescriptorValidation
{
    public static final String CONFLUENCE_SCHEMA = "/schema/confluence-schema.json";

    private JsonDescriptorValidator validator;

    @Before
    public void setup() throws Exception
    {
        ConnectSchemaLocator schemaLocator = mock(ConnectSchemaLocator.class);
        String schema = IOUtils.toString(getClass().getResourceAsStream(CONFLUENCE_SCHEMA));
        when(schemaLocator.getSchema(any(ProductFilter.class))).thenReturn(schema);

        validator = new JsonDescriptorValidator(schemaLocator);
    }

    @Test
    public void testGoodConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("addonMultipleCapabilities.json");
        DescriptorValidationResult result = validator.validate(json);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testBadConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("issueTabAddon.json");
        DescriptorValidationResult result = validator.validate(json);

        assertFalse(result.isSuccess());
    }
}
