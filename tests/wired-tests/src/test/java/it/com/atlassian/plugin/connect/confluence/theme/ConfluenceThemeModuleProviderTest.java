package it.com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.ThemeModuleDescriptor;
import com.atlassian.confluence.themes.ThemedDecorator;
import com.atlassian.confluence.themes.VelocityResultOverride;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.theme.ConfluenceRemoteAddonTheme;
import com.atlassian.plugin.connect.confluence.theme.ConfluenceThemeModuleProvider;
import com.atlassian.plugin.connect.confluence.theme.NavigationTargetOverrideInfo;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean.newConfluenceThemeModuleBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteBean.newConfluenceThemeRouteBeanBuilder;
import static com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean.newConfluenceThemeRouteInterceptionsBeanBuilder;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 *
 */
@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceThemeModuleProviderTest {
    private static final String ADDON_KEY = "addon-with-confluence-themes-test";
    private static final String THEME_KEY = "test-theme-1";
    private static final String ADDON_BASE_URL = "http://theme.example.com";
    private static final String THEME_ICON = "/icon.png";
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    private final ConfluenceThemeModuleProvider confluenceThemeModuleProvider;

    public ConfluenceThemeModuleProviderTest(TestPluginInstaller testPluginInstaller,
                                             TestAuthenticator testAuthenticator,
                                             ConfluenceThemeModuleProvider confluenceThemeModuleProvider) {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.confluenceThemeModuleProvider = confluenceThemeModuleProvider;
    }

    @BeforeClass
    public void setup() {
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void createConnectThemeModule() throws Exception {
        ConfluenceThemeModuleBean themeBean = newConfluenceThemeModuleBean()
                .withIcon(makeIcon())
                .withKey(THEME_KEY)
                .withName(makeName())
                .withRoutes(makeRoutes())
                .build();
        ConnectAddonBean addon = newConnectAddonBean()
                .withName("addon-with-confluence-themes-test")
                .withKey(ADDON_KEY)
                .withBaseurl(ADDON_BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("confluenceThemes", themeBean)
                .build();

        Plugin plugin = null;
        try {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors =
                    confluenceThemeModuleProvider.createPluginModuleDescriptors(newArrayList(themeBean), addon);
            //1 theme module + each route is one layout module (4 routes atm)
            assertThat("incorrect number of module descriptors found", descriptors.size(), is(1 + NavigationTargetOverrideInfo.values().length));
            ModuleDescriptor last = descriptors.get(descriptors.size() - 1);
            assertThat("incorrect type of module descriptor :" + last.getClass().getName(), last, instanceOf(ThemeModuleDescriptor.class));
            ThemeModuleDescriptor themeDescriptor = (ThemeModuleDescriptor) last;
            assertThat(themeDescriptor.getBodyClass(), is(ConfluenceRemoteAddonTheme.class.getName()));

            List<ResourceDescriptor> themeResourceDescriptors = themeDescriptor.getResourceDescriptors();
            assertThat("should only have 1 resource descriptor", themeResourceDescriptors.size(), is(1));
            assertThat(themeResourceDescriptors.get(0).getName(), is("themeicon.gif"));
            assertThat(themeResourceDescriptors.get(0).getLocation(), is(ADDON_BASE_URL + THEME_ICON));

            Map<String, String> params = themeDescriptor.getParams();

            assertThat("no url in the theme param for blogpost", params.containsKey("theme-url-blogpost"), is(true));
            assertThat("no url in the theme param for blogpost", params.get("theme-url-blogpost"), is("/page"));

            assertThat("no url in the theme param for page", params.containsKey("theme-url-page"), is(true));
            assertThat("no url in the theme param for page", params.get("theme-url-page"), is("/page"));

            assertThat("no url in the theme param for dashboard", params.containsKey("theme-url-dashboard"), is(true));
            assertThat("no url in the theme param for dashboard", params.get("theme-url-dashboard"), is("/dashboard"));

            assertThat("no url in the theme param for space", params.containsKey("theme-url-space"), is(true));
            assertThat("no url in the theme param for space", params.get("theme-url-space"), is("/space"));

            List<VelocityResultOverride> velocityResultOverrides = themeDescriptor.getVelocityResultOverrides();
            assertThat("wrong number of velocity overrides", velocityResultOverrides.size(), is(4));
            List<ThemedDecorator> layouts = themeDescriptor.getLayouts();
            assertThat("wrong number of layouts", layouts.size(), is(4));
            List<ModuleDescriptor> layoutModules = descriptors.subList(0, descriptors.size() - 1);
            assertThat("mismatched layout modules", layoutModules.size(), is(layouts.size()));

            assertThat(params.get("addon-key"), is(ADDON_KEY));
            assertThat(params.get("theme-key"), is(THEME_KEY));


        } finally {
            if (null != plugin) {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }

    private I18nProperty makeName() {
        return new I18nProperty("test theme 1", "test theme 1");
    }

    private IconBean makeIcon() {
        return IconBean.newIconBean().withUrl(THEME_ICON).withHeight(90).withWidth(90).build();
    }

    private ConfluenceThemeRouteInterceptionsBean makeRoutes() {
        ConfluenceThemeRouteBean dashboardRoute = newConfluenceThemeRouteBeanBuilder().withUrl("/dashboard").build();
        ConfluenceThemeRouteBean contentviewRoute = newConfluenceThemeRouteBeanBuilder().withUrl("/page").build();
        ConfluenceThemeRouteBean spaceviewRoute = newConfluenceThemeRouteBeanBuilder().withUrl("/space").build();
        return newConfluenceThemeRouteInterceptionsBeanBuilder()
                .withDashboard(dashboardRoute)
                .withContentview(contentviewRoute)
                .withSpaceview(spaceviewRoute)
                .build();
    }
}
