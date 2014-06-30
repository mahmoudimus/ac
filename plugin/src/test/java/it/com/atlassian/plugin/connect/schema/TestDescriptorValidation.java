package it.com.atlassian.plugin.connect.schema;

import com.atlassian.plugin.connect.modules.schema.ConnectDescriptorValidator;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith (AtlassianPluginsTestRunner.class)
public class TestDescriptorValidation
{
    private ConnectDescriptorValidator validator;
    private ConnectSchemaLocator schemaLocator;

    public TestDescriptorValidation(final ConnectDescriptorValidator validator, final ConnectSchemaLocator schemaLocator)
    {
        this.validator = validator;
        this.schemaLocator = schemaLocator;
    }

    @Test
    public void testGoodConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("validGenericDescriptor.json");
        DescriptorValidationResult result = validator.validate(json, schemaLocator.getSchemaForCurrentProduct());

        assertTrue(result.isValid());
    }

    @Test
    public void testBadConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("invalidGenericDescriptor.json");
        DescriptorValidationResult result = validator.validate(json, schemaLocator.getSchemaForCurrentProduct());

        assertFalse(result.isValid());
    }

    @Test
    public void emptyKey() throws Exception
    {
        String json = readAddonTestFile("emptyKey.json");
        DescriptorValidationResult result = validator.validate(json, schemaLocator.getSchemaForCurrentProduct());

        assertFalse(result.isValid());
    }

    @Test
    public void invalidWebitemStyles() throws Exception
    {
        String json = readAddonTestFile("webitem/invalidStylesWebItemTest.json");
        DescriptorValidationResult result = validator.validate(json, schemaLocator.getSchemaForCurrentProduct());

        assertFalse(result.isValid());
    }
}
