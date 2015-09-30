package it.com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static it.com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;

@RunWith (AtlassianPluginsTestRunner.class)
public class TestDescriptorValidation
{
    private ConnectAddonBeanFactory connectAddonBeanFactory;

    public TestDescriptorValidation(final ConnectAddonBeanFactory connectAddonBeanFactory)
    {
        this.connectAddonBeanFactory = connectAddonBeanFactory;
    }

    @Test
    public void testGoodConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("validGenericDescriptor.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void testBadConfluenceDescriptor() throws Exception
    {
        String json = readAddonTestFile("invalidGenericDescriptor.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void emptyKey() throws Exception
    {
        String json = readAddonTestFile("emptyKey.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void invalidWebitemStyles() throws Exception
    {
        String json = readAddonTestFile("webitem/invalidStylesWebItemTest.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test
    public void testGoodDescriptorWithPluginProvidedModule() throws Exception
    {
        String json = readAddonTestFile("descriptorWithPluginProvidedModule.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void invalidModuleType() throws Exception
    {
        String json = readAddonTestFile("descriptorWithUnknownModuleType.json");
        connectAddonBeanFactory.fromJson(json);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void moduleValidationFails() throws Exception
    {
        String json = readAddonTestFile("descriptorWithInvalidModuleBody.json");
        connectAddonBeanFactory.fromJson(json);
    }
}
