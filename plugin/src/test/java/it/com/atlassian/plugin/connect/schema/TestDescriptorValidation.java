package it.com.atlassian.plugin.connect.schema;

import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectDescriptorValidator;
import com.atlassian.plugin.connect.plugin.capabilities.schema.DescriptorValidationResult;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith (AtlassianPluginsTestRunner.class)
public class TestDescriptorValidation
{
    private ConnectDescriptorValidator validator;

    public TestDescriptorValidation(final ConnectDescriptorValidator validator)
    {
        this.validator = validator;
    }

    @Test
    public void testGoodConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("validConfluenceDescriptor.json");
        DescriptorValidationResult result = validator.validate(json);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testBadConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("validJiraDescriptor.json");
        DescriptorValidationResult result = validator.validate(json);

        assertFalse(result.isSuccess());
    }
}
