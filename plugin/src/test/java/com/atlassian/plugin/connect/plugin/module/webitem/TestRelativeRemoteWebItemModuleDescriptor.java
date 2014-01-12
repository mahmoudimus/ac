package com.atlassian.plugin.connect.plugin.module.webitem;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
