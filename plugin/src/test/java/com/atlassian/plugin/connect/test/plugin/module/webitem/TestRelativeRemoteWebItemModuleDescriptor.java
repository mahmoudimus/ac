package com.atlassian.plugin.connect.test.plugin.module.webitem;

import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class TestRelativeRemoteWebItemModuleDescriptor extends RemoteWebItemModuleDescriptorTestBase
{
    @Override
    protected String getInputLinkText()
    {
        return "my_page_id={page.id}";
    }

    protected String getExpectedUrl()
    {
        return "/plugins/servlet/atlassian-connect/null/module-key?" + getInputLinkText();
    }
}
