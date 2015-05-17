package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.validate.impl.PageConditionsValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.message.I18nResolver;
import com.opensymphony.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageConditionValidationTest
{
    private
    @Mock
    I18nResolver i18nResolver;

    private PageConditionsValidator conditionsValidator;

    @Before
    public void setup()
    {
        when(i18nResolver.getText(anyString(), anyString(), anyString())).thenReturn("error message");

        this.conditionsValidator = new PageConditionsValidator(i18nResolver);
    }

    @Test
    public void validBuiltInConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-builtin-condition.json"));
    }

    @Test
    public void validRemoteConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-remote-condition.json"));
    }

    @Test
    public void validNestedBuiltInConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-nested-builtin-condition.json"));
    }

    @Test
    public void validNestedRemoteConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-nested-remote-condition.json"));
    }

    @Test(expected = InvalidDescriptorException.class)
    public void invalidConditionFails() throws Exception
    {
        validateFully(readConditionJson("invalid-condition.json"));
    }

    @Test(expected = InvalidDescriptorException.class)
    public void invalidNestedConditionFails() throws Exception
    {
        validateFully(readConditionJson("invalid-nested-condition.json"));
    }

    private String readConditionJson(String filename) throws IOException
    {
        return readAddonTestFile("pageconditions/" + filename);
    }

    public void validateFully(final String jsonDescriptor) throws Exception
    {

        ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(jsonDescriptor, ConnectAddonBean.class);

        conditionsValidator.validate(addon);
    }

    private String getSchema() throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/schema/jira-schema.json").getFile());
    }
}