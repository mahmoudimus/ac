package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static it.TestConstants.BETTY_USERNAME;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertTrue;

public class TestFileInstall extends ConnectWebDriverTestBase
{
    @Test
    @Ignore
    public void testGeneralPage() throws Exception
    {
        String baseUrl = product.getProductInstance().getBaseUrl();
        File base = new File(new File(new File("target"), "tmp"), "file-install-base");
        base.mkdirs();
        cleanDirectory(base);
        String descriptor =
                "key: file-app\n" +
                        "name: File App\n" +
                        "version: 1\n" +
                        "general-page:\n" +
                        "  - key: first\n" +
                        "    name: First (file)\n" +
                        "    url: /first.html\n";
        File descriptorFile = new File(base, "descriptor.yaml");
        FileUtils.writeStringToFile(descriptorFile, descriptor);
        FileUtils.writeStringToFile(new File(base, "first.html"),
                HttpUtils.render("iframe-hello-world.mu",
                        Collections.<String, Object>singletonMap(
                                "baseurl", baseUrl)));

        AtlassianConnectRestClient client = new AtlassianConnectRestClient(
                baseUrl, "admin", "admin");
        client.install(descriptorFile.toURI().toString());

        loginAsBetty();
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "first",
                "First (file)");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.isLoaded());
        client.uninstall("file-app");
    }
}
