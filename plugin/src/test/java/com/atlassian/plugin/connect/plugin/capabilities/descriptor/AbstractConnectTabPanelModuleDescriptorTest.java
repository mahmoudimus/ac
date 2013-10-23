package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.util.TestContextBuilder;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.util.TestMatchers.hasIFramePath;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

public abstract class AbstractConnectTabPanelModuleDescriptorTest<T extends TabPanel>
{
    private BrowseComponentContext browseComponentContext;

    @Before
    public void beforeEachTest()
    {
        browseComponentContext = TestContextBuilder.buildBrowseComponentContext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rendersWithCorrectUrl() throws IOException
    {
        createDescriptor().getModule().getHtml(browseComponentContext);
        String expectedUrl = getRawUrl().replace("${project.id}", String.valueOf(TestContextBuilder.PROJECT_ID)).replace("${project.key}", TestContextBuilder.PROJECT_KEY);
        verify(getIFrameRenderer()).render(argThat(hasIFramePath(expectedUrl)), anyString());
    }

    protected abstract TabPanelModuleDescriptor<T> createDescriptor() throws IOException;
    protected abstract IFrameRenderer getIFrameRenderer();
    protected abstract String getRawUrl();
}
