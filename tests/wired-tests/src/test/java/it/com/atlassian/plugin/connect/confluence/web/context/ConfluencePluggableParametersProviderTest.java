package it.com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.capabilities.provider.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.ImmutableMap;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public final class ConfluencePluggableParametersProviderTest extends AbstractConnectAddonTest
{
    public ConfluencePluggableParametersProviderTest(final WebItemModuleProvider webItemModuleProvider, final TestPluginInstaller testPluginInstaller, final TestAuthenticator testAuthenticator)
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

    private String registerWebItemWithProjectInContextAndGetUrl() throws IOException
    {
        WebItemModuleDescriptor descriptor = registerWebItem("customProperty=${space.keyConcatId}", "atl.admin/menu");
        Map<String, Object> context = ImmutableMap.<String, Object>of("space", space(42L, "key"));
        return descriptor.getLink().getDisplayableUrl(servletRequest, context);
    }

    private Space space(long id, String key)
    {
        Space result = mock(Space.class);
        when(result.getKey()).thenReturn(key);
        when(result.getId()).thenReturn(id);
        return result;
    }
}
