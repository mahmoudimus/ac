package it.com.atlassian.plugin.connect.confluence.web;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.api.web.redirect.RedirectServletPath;
import com.atlassian.plugin.connect.plugin.web.item.WebItemModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.AbstractConnectAddonTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceWebItemModuleProviderTest extends AbstractConnectAddonTest {
    public static final String SPACE_KEY = "ds";

    public ConfluenceWebItemModuleProviderTest(WebItemModuleProvider webItemModuleProvider, TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator) {
        super(webItemModuleProvider, testPluginInstaller, testAuthenticator);
    }

    @Test
    public void singleAddonLinkWithContextPrams() throws Exception {
        WebItemModuleDescriptor descriptor = registerWebItem("mySpace={space.key}", "atl.admin/menu");

        Map<String, Object> context = new HashMap<>();
        Page page = mock(Page.class);
        Space space = mock(Space.class);
        WebInterfaceContext wic = mock(WebInterfaceContext.class);

        when(space.getId()).thenReturn(1234L);
        when(space.getKey()).thenReturn(SPACE_KEY);

        when(wic.getSpace()).thenReturn(space);
        when(wic.getPage()).thenReturn(page);

        context.put("webInterfaceContext", wic);

        String convertedUrl = descriptor.getLink().getDisplayableUrl(servletRequest, context);

        assertThat(convertedUrl, containsString(RedirectServletPath.forModule(PLUGIN_KEY, MODULE_KEY)));
        assertThat(convertedUrl, containsString("space.key=" + SPACE_KEY));
    }
}
