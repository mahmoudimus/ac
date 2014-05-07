package it.com.atlassian.plugin.connect.installer;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.connect.modules.beans.AuthenticationBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.TestAuthenticator;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

@RunWith(AtlassianPluginsTestRunner.class)
public class OneHundredInstallTest
{
    public static final String MODULE_NAME = "My Web Item - ";
    public static final String MODULE_KEY = "my-web-item-";
    public static final String INSTALL_TOTAL = "installtotal";
    public static final String INSTALL_AVG = "installavg";
    public static final String DISABLE_TOTAL = "disabletotal";
    public static final String DISABLE_AVG = "disableavg";
    public static final String ENABLE_TOTAL = "enabletotal";
    public static final String ENABLE_AVG = "enableavg";
    public static final String UNINSTALL_TOTAL = "uninstalltotal";
    public static final String UNINSTALL_AVG = "uninstallavg";

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;

    public OneHundredInstallTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator) 
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
    }

    @Before
    public void setUp() throws IOException
    {
        testAuthenticator.authenticateUser("admin");
    }
    
    @Test
    public void lifecycleForOneHundredAddons() throws IOException
    {
        Map<String,Long> opTimes = new HashMap<String, Long>();
        
        List<Long> installTimes = new ArrayList<Long>();
        List<Long> disableTimes = new ArrayList<Long>();
        List<Long> enableTimes = new ArrayList<Long>();
        List<Long> uninstallTimes = new ArrayList<Long>();
        
        Set<Plugin> addons = new HashSet<Plugin>();

        /*
         *************    INSTALL   ******************
         */
        long installStart = System.currentTimeMillis();
        for(int i=0;i<100;i++)
        {
            ConnectAddonBean addon = createAddon(i);

            long addonInstallStart = System.currentTimeMillis();
            addons.add(testPluginInstaller.installAddon(addon));
            long addonInstallEnd = System.currentTimeMillis();
            
            installTimes.add((addonInstallEnd - addonInstallStart));
        }
        long installEnd = System.currentTimeMillis();
        
        opTimes.put(INSTALL_TOTAL,(installEnd - installStart));
        opTimes.put(INSTALL_AVG,calculateAverage(installTimes));


        /*
         *************    DISABLE   ******************
         */
        long disableStart = System.currentTimeMillis();
        for(Plugin disableAddon : addons)
        {
            long addonDisableStart = System.currentTimeMillis();
            testPluginInstaller.disableAddon(disableAddon.getKey());
            long addonDisableEnd = System.currentTimeMillis();

            disableTimes.add((addonDisableEnd - addonDisableStart));
        }
        long disableEnd = System.currentTimeMillis();

        opTimes.put(DISABLE_TOTAL,(disableEnd - disableStart));
        opTimes.put(DISABLE_AVG,calculateAverage(disableTimes));
        
        /*
         *************    ENABLE   ******************
         */
        long enableStart = System.currentTimeMillis();
        for(Plugin enableAddon : addons)
        {
            long addonEnableStart = System.currentTimeMillis();
            testPluginInstaller.enableAddon(enableAddon.getKey());
            long addonEnableEnd = System.currentTimeMillis();

            enableTimes.add((addonEnableEnd - addonEnableStart));
        }
        long enableEnd = System.currentTimeMillis();

        opTimes.put(ENABLE_TOTAL,(enableEnd - enableStart));
        opTimes.put(ENABLE_AVG,calculateAverage(enableTimes));
        
        /*
         *************    UNINSTALL   ******************
         */
        long deleteStart = System.currentTimeMillis();
        for(Plugin deleteAddon : addons)
        {
            long addonDeleteStart = System.currentTimeMillis();
            testPluginInstaller.uninstallAddon(deleteAddon);
            long addonDeleteEnd = System.currentTimeMillis();

            uninstallTimes.add((addonDeleteEnd - addonDeleteStart));
        }
        long deleteEnd = System.currentTimeMillis();

        opTimes.put(UNINSTALL_TOTAL,(deleteEnd - deleteStart));
        opTimes.put(UNINSTALL_AVG,calculateAverage(uninstallTimes));

        System.out.println("************* JSON ADDON *************");
        for(Map.Entry<String,Long> entry : opTimes.entrySet())
        {
            System.out.println(entry.getKey() + " = " + entry.getValue() + " ms");
        }
    }

    @Test
    public void lifecycleForOneHundredPlugins() throws IOException
    {
        Map<String,Long> opTimes = new HashMap<String, Long>();

        List<Long> installTimes = new ArrayList<Long>();
        List<Long> disableTimes = new ArrayList<Long>();
        List<Long> enableTimes = new ArrayList<Long>();
        List<Long> uninstallTimes = new ArrayList<Long>();

        Set<Plugin> addons = new HashSet<Plugin>();

        /*
         *************    INSTALL   ******************
         */
        long installStart = System.currentTimeMillis();
        for(int i=0;i<100;i++)
        {
            File pluginFile = createPlugin(i);

            long addonInstallStart = System.currentTimeMillis();
            addons.add(testPluginInstaller.installPlugin(pluginFile));
            long addonInstallEnd = System.currentTimeMillis();

            installTimes.add((addonInstallEnd - addonInstallStart));
        }
        long installEnd = System.currentTimeMillis();

        opTimes.put(INSTALL_TOTAL,(installEnd - installStart));
        opTimes.put(INSTALL_AVG,calculateAverage(installTimes));


        /*
         *************    DISABLE   ******************
         */
        long disableStart = System.currentTimeMillis();
        for(Plugin disableAddon : addons)
        {
            long addonDisableStart = System.currentTimeMillis();
            testPluginInstaller.disablePlugin(disableAddon.getKey());
            long addonDisableEnd = System.currentTimeMillis();

            disableTimes.add((addonDisableEnd - addonDisableStart));
        }
        long disableEnd = System.currentTimeMillis();

        opTimes.put(DISABLE_TOTAL,(disableEnd - disableStart));
        opTimes.put(DISABLE_AVG,calculateAverage(disableTimes));
        
        /*
         *************    ENABLE   ******************
         */
        long enableStart = System.currentTimeMillis();
        for(Plugin enableAddon : addons)
        {
            long addonEnableStart = System.currentTimeMillis();
            testPluginInstaller.enablePlugin(enableAddon.getKey());
            long addonEnableEnd = System.currentTimeMillis();

            enableTimes.add((addonEnableEnd - addonEnableStart));
        }
        long enableEnd = System.currentTimeMillis();

        opTimes.put(ENABLE_TOTAL,(enableEnd - enableStart));
        opTimes.put(ENABLE_AVG,calculateAverage(enableTimes));
        
        /*
         *************    UNINSTALL   ******************
         */
        long deleteStart = System.currentTimeMillis();
        for(Plugin deleteAddon : addons)
        {
            long addonDeleteStart = System.currentTimeMillis();
            testPluginInstaller.uninstallPlugin(deleteAddon);
            long addonDeleteEnd = System.currentTimeMillis();

            uninstallTimes.add((addonDeleteEnd - addonDeleteStart));
        }
        long deleteEnd = System.currentTimeMillis();

        opTimes.put(UNINSTALL_TOTAL,(deleteEnd - deleteStart));
        opTimes.put(UNINSTALL_AVG,calculateAverage(uninstallTimes));

        System.out.println("************* TRADITIONAL PLUGIN *************");
        for(Map.Entry<String,Long> entry : opTimes.entrySet())
        {
            System.out.println(entry.getKey() + " = " + entry.getValue() + " ms");
        }
    }

    private Long calculateAverage(List<Long> installTimes)
    {
        long total = 0;
        
        for(Long time : installTimes)
        {
            total += time;
        }
        
        return total / installTimes.size();
    }

    private File createPlugin(int index)
    {
        String pluginKey = randomPluginKey();
        
        final Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("atlassian-plugin");
        doc.setRootElement(root);

        root.addAttribute("key",pluginKey)
                .addAttribute("name",pluginKey)
                .addAttribute("plugins-version","2");
        
        Element pluginInfo = root.addElement("plugin-info");
        pluginInfo.addElement("description","a plugin");
        pluginInfo.addElement("vendor","Atlassian");
        pluginInfo.addElement("version", "1.0");

        for(int i=0;i<10;i++)
        {
            Element webItem = root.addElement("web-item");
            webItem.addAttribute("key",MODULE_KEY + index + "-" + i)
                    .addAttribute("section","system.top.navigation");
            
            Element label = webItem.addElement("label");
            label.addAttribute("key", "some.label");

            Element link = webItem.addElement("link");
            link.setText("http://www.atlassian.com");
        }

        return ZipBuilder.buildZip("install-" + pluginKey, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, doc);
            }
        });
    }
    
    private ConnectAddonBean createAddon(int index)
    {
        String pluginKey = randomPluginKey();
        
        List<WebItemModuleBean> webitems = new ArrayList<WebItemModuleBean>();
        
        for(int i=0;i<10;i++)
        {
            webitems.add(newWebItemBean()
                    .withName(new I18nProperty(MODULE_NAME + index + "-" + i, ""))
                    .withKey(MODULE_KEY + index + "-" + i)
                    .withUrl("/my/addon")
                    .withLocation("atl.admin/menu")
                    .build());
        }

        ConnectAddonBean addon = newConnectAddonBean()
                .withName(pluginKey)
                .withKey(pluginKey)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(pluginKey))
                .withAuthentication(AuthenticationBean.none())
                .withModules("webItems", webitems.toArray(new WebItemModuleBean[0]))
                .build();

        return addon;
                
    }

    public static String randomPluginKey()
    {
        return RandomStringUtils.randomAlphanumeric(20).replaceAll("3", "4").toLowerCase();
    }
}
