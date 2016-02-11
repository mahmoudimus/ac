package it.com.atlassian.plugin.connect.confluence.blueprint;

import java.util.List;

import com.atlassian.confluence.plugins.createcontent.extensions.BlueprintModuleDescriptor;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.blueprint.BlueprintModuleProvider;
import com.atlassian.plugin.connect.confluence.blueprint.BlueprintContextProvider;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.IconBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.CreateResultType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class BlueprintModuleProviderTest
{

    public static final String PLUGIN_KEY = "blueprints-plugin";
    public static final String PLUGIN_NAME = "Blueprints Plugin";
    public static final String MODULE_NAME = "My Blueprint";
    public static final String MODULE_KEY = "my-blueprint";
    public static final String BASE_URL = "http://my.connect.addon.com";

    private final BlueprintModuleProvider blueprintModuleProvider;
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    public BlueprintModuleProviderTest(BlueprintModuleProvider blueprintModuleProvider,
                                       TestPluginInstaller testPluginInstaller,
                                       TestAuthenticator testAuthenticator)
    {
        this.blueprintModuleProvider = blueprintModuleProvider;
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @BeforeClass
    public void setup()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @Test
    public void createBlueprintModules() throws Exception
    {
        BlueprintTemplateBean template = new BlueprintTemplateBeanBuilder()
                .withUrl("/blueprints/blueprint.xml")
                .withBlueprintContextUrl("/blueprints/context")
                .build();

        BlueprintModuleBean bean = newBlueprintModuleBean()
                .withName(new I18nProperty(MODULE_NAME, ""))
                .withKey(MODULE_KEY)
                .withTemplate(template)
                .withCreateResult(CreateResultType.VIEW)
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

        try
        {
            plugin = testPluginInstaller.installAddon(addon);

            List<ModuleDescriptor> descriptors = blueprintModuleProvider.createPluginModuleDescriptors(newArrayList(bean), addon);

            // should get a WebItem Descriptor and a Blueprint Descriptor
            assertEquals(3, descriptors.size());

            // check the web item descriptor
            WebItemModuleDescriptor webItemDescriptor = (WebItemModuleDescriptor) descriptors.get(0);

            String baseAddonKey = ModuleKeyUtils.addonAndModuleKey(PLUGIN_KEY, MODULE_KEY);

            assertEquals(baseAddonKey + "-web-item", webItemDescriptor.getKey());
            assertEquals(MODULE_NAME, webItemDescriptor.getI18nNameKey());
            assertEquals("system.create.dialog/content", webItemDescriptor.getSection());

//            See: https://ecosystem.atlassian.net/browse/CE-19
//            ResourceDescriptor iconResourceDescriptor = webItemDescriptor.getResourceDescriptor("download", "icon");
//            assertNotNull(iconResourceDescriptor);
//            assertEquals("web-item-icon-resource-location", iconResourceDescriptor.getLocation());

            String blueprintKey = webItemDescriptor.getParams().get("blueprintKey");
            assertNotNull(blueprintKey);
            assertEquals(baseAddonKey + "-blueprint", blueprintKey);

            webItemDescriptor.enabled();

            // check the content template descriptor
            ContentTemplateModuleDescriptor contentTemplateModuleDescriptor = (ContentTemplateModuleDescriptor) descriptors.get(1);
            assertNotNull(contentTemplateModuleDescriptor);

            assertEquals(baseAddonKey + "-content-template", contentTemplateModuleDescriptor.getKey());
            assertEquals(MODULE_NAME, contentTemplateModuleDescriptor.getI18nNameKey());
            assertEquals(BASE_URL + "/blueprints/blueprint.xml", contentTemplateModuleDescriptor.getResourceDescriptor("download", "template").getLocation());

            contentTemplateModuleDescriptor.enabled();
            //context provider is only available after the module is enabled.
            assertTrue("ConnectBlueprintContextProvider not returned from getContextProvider" , contentTemplateModuleDescriptor.getContextProvider() instanceof BlueprintContextProvider);
            BlueprintContextProvider context = (BlueprintContextProvider) contentTemplateModuleDescriptor.getContextProvider();
            assertEquals("the context url either doesn't match, or needs to be a relative url", "/blueprints/context", context.getContextUrl());
            assertEquals(PLUGIN_KEY, context.getAddonKey());
            assertEquals(MODULE_KEY, context.getBlueprintKey());

            // check the blueprint descriptor
            BlueprintModuleDescriptor blueprintModuleDescriptor = (BlueprintModuleDescriptor) descriptors.get(2);
            assertNotNull(blueprintModuleDescriptor);
            assertEquals("view", blueprintModuleDescriptor.getCreateResult());

            blueprintModuleDescriptor.enabled();

        }
        finally
        {
            if (null != plugin)
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
        }
    }


}
