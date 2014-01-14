package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.TestContextBuilder;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.matchers.IFrameContextMatchers.hasIFramePath;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

public abstract class AbstractConnectTabPanelModuleDescriptorTest<T extends TabPanel>
{
    private BrowseContext browseComponentContext;

    @Before
    public void beforeEachTest()
    {
        browseComponentContext = createBrowseContext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rendersWithCorrectUrl() throws IOException
    {
        createDescriptor().getModule().getHtml(browseComponentContext);
        String expectedUrl = getRawUrl().replace("{project.id}", String.valueOf(TestContextBuilder.PROJECT_ID)).replace("{project.key}", TestContextBuilder.PROJECT_KEY);
        verify(getIFrameRenderer()).render(argThat(hasIFramePath(expectedUrl)), anyString());
    }

    protected abstract TabPanelModuleDescriptor<T> createDescriptor() throws IOException;
    protected abstract IFrameRenderer getIFrameRenderer();
    protected abstract String getRawUrl();

    /**
     * Generate the argument to {@link TabPanel#getHtml(com.atlassian.jira.project.browse.BrowseContext)}.
     * The super-class implementation is sufficient for most cases.
     * Override this method to provide a different {@link BrowseContext} implementation.
     * @return a {@link BrowseContext} sub-class.
     */
    protected BrowseContext createBrowseContext()
    {
        return TestContextBuilder.buildBrowseComponentContext("addon-key");
    }
}
