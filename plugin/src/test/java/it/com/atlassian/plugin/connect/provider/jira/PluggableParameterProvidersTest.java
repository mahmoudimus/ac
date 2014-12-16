package it.com.atlassian.plugin.connect.provider.jira;

import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public final class PluggableParameterProvidersTest extends AbstractConnectAddonTest
{
    public PluggableParameterProvidersTest(final WebItemModuleProvider webItemModuleProvider, final TestPluginInstaller testPluginInstaller, final TestAuthenticator testAuthenticator)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void parametersExtractedByPluginAreAvailableForWebItemsUrl() throws IOException
    {
        String url = registerWebItemWithProjectInContextAndGetUrl();
        assertStringContains(url, "customProperty=key42");
    }

    @Test
    public void permissionChecksFromPluginsAreRespected() throws IOException
    {
        actAsAnonymous();
        String url = registerWebItemWithProjectInContextAndGetUrl();
        assertStringContains(url, "customProperty=&");
    }

    // Equivalent to assertThat(hay, containsString(needle));
    // Hamcrest matchers throw LinkageError for some reason so we need to do this like that.
    private void assertStringContains(String hay, String needle)
    {
        assertTrue("expected: contains '" + needle + " ', actual: " + hay, hay.contains(needle));
    }

    private String registerWebItemWithProjectInContextAndGetUrl() throws IOException
    {
        WebItemModuleDescriptor descriptor = registerWebItem("customProperty=${project.keyConcatId}", "atl.admin/menu");

        Map<String, Object> context = ImmutableMap.<String, Object>of("project", project(42L, "key"));

        return descriptor.getLink().getDisplayableUrl(servletRequest, context);
    }

}
