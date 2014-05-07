package it.com.atlassian.plugin.connect.provider.confluence;

import com.atlassian.confluence.plugins.createcontent.extensions.BlueprintModuleDescriptor;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateBeanBulder;
import com.atlassian.plugin.connect.modules.beans.builder.IconBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.BlueprintModuleProvider;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceBlueprintModuleProviderTest {

    public static final String CONTEXT_PATH = "http://ondemand.com/confluence";
    public static final String PLUGIN_KEY = "blueprints-plugin";
    public static final String PLUGIN_NAME = "Blueprints Plugin";
    public static final String MODULE_NAME = "My Blueprint";
    public static final String MODULE_KEY = "my-blueprint";
    public static final String BASE_URL = "http://my.connect.addon.com";
    public static final String SPACE_KEY = "ds";

    private final BlueprintModuleProvider blueprintModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    public ConfluenceBlueprintModuleProviderTest(BlueprintModuleProvider blueprintModuleProvider,
                                                 TestPluginInstaller testPluginInstaller,
                                                 TestAuthenticator testAuthenticator) {
        this.blueprintModuleProvider = blueprintModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup() {
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void createBlueprintModules() throws Exception {
        //System.out.println("session user: " + userManager.getRemoteUser().getUsername());
        BlueprintModuleBean bean = BlueprintModuleBean.newBlueprintModuleBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withTemplate(new BlueprintTemplateBeanBulder().withUrl("/blueprints/blueprint.xml").build())
                .withIcon(new IconBeanBuilder().withUrl("/blueprints/blueprints.png").build())
                .build();


        ConnectAddonBean addon = newConnectAddonBean()
                .withName(PLUGIN_NAME)
                .withKey(PLUGIN_KEY)
                .withBaseurl(BASE_URL)
                .withAuthentication(AuthenticationBean.none())
                .withModules("blueprints", bean)
                .build();

        Plugin plugin = null;

        try {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = blueprintModuleProvider.provideModules(addon, plugin, "blueprints", newArrayList(bean));

            // should get a WebItem Descriptor and a Blueprint Descriptor
            assertEquals(3, descriptors.size());

            // check the web item descriptor
            WebItemModuleDescriptor webItemDescriptor = (WebItemModuleDescriptor) descriptors.get(0);

            assertEquals(PLUGIN_KEY + "__" + MODULE_KEY + "-web-item", webItemDescriptor.getKey());
            assertEquals(MODULE_NAME, webItemDescriptor.getI18nNameKey());
            assertEquals("system.create.dialog/content",webItemDescriptor.getSection());
            ResourceDescriptor iconResourceDescriptor = webItemDescriptor.getResourceDescriptor("download", "icon");
            assertNotNull(iconResourceDescriptor);
            assertEquals("web-item-icon-resource-location",iconResourceDescriptor.getLocation()); //TODO

            String blueprintKey = webItemDescriptor.getParams().get("blueprintKey");
            assertNotNull(blueprintKey);
            assertEquals(PLUGIN_KEY + "__" + MODULE_KEY + "-web-item", blueprintKey);

            webItemDescriptor.enabled();

            // check the content template descriptor
            ContentTemplateModuleDescriptor contentTemplateModuleDescriptor = (ContentTemplateModuleDescriptor) descriptors.get(1);
            assertNotNull(contentTemplateModuleDescriptor);

            assertEquals(PLUGIN_KEY + "__" + MODULE_KEY + "-content-template", contentTemplateModuleDescriptor.getKey());
            assertEquals(MODULE_NAME, contentTemplateModuleDescriptor.getI18nNameKey());
            assertEquals(BASE_URL+"/blueprints/blueprint.xml",contentTemplateModuleDescriptor.getResourceDescriptor("download","template").getLocation());

            contentTemplateModuleDescriptor.enabled();

            // check the blueprint descriptor
            BlueprintModuleDescriptor blueprintModuleDescriptor = (BlueprintModuleDescriptor) descriptors.get(2);
            assertNotNull(blueprintModuleDescriptor);

            // TODO

            blueprintModuleDescriptor.enabled();

        } finally {
            if (null != plugin) {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }


}
