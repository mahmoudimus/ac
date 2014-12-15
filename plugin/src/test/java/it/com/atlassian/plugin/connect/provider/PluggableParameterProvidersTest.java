package it.com.atlassian.plugin.connect.provider;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.AbstractConnectAddonTest;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Application ("jira")
@RunWith (AtlassianPluginsTestRunner.class)
public final class PluggableParameterProvidersTest extends AbstractConnectAddonTest
{
    public PluggableParameterProvidersTest(final WebItemModuleProvider webItemModuleProvider, final TestPluginInstaller testPluginInstaller, final TestAuthenticator testAuthenticator)
    {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
    }


    public void parametersExtractedByPluginAreAvailableForWebItemsUrl() throws IOException
    {
        String url = registerWebItemWithProjectInContextAndGetUrl();
        assertThat(url, containsString("customProperty=42key"));
    }

    public void permissionChecksFromPluginsAreRespected() throws IOException
    {
        actAsAnonymous();
        String url = registerWebItemWithProjectInContextAndGetUrl();
        assertThat(url, containsString("customProperty=${project.keyConcatId}"));
    }

    private String registerWebItemWithProjectInContextAndGetUrl() throws IOException
    {
        WebItemModuleDescriptor descriptor = registerWebItem("customProperty=${project.keyConcatId}", "atl.admin/menu");

        Map<String, Object> context = ImmutableMap.<String, Object>of("project", new MockProject(42L, "key"));

        return descriptor.getLink().getDisplayableUrl(servletRequest, context);
    }

}
