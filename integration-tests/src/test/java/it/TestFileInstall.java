package it;

import com.atlassian.plugin.remotable.test.*;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.remotable.test.pageobjects.GeneralPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginTestPage;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertTrue;

public class TestFileInstall extends AbstractRemotablePluginTest
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
                HttpUtils.render("hello-world-page.mu",
                        Collections.<String, Object>singletonMap(
                        "baseurl", baseUrl)));

        AtlassianConnectRestClient client = new AtlassianConnectRestClient(
                baseUrl, "admin", "admin");
        client.install(descriptorFile.toURI().toString());

        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "first",
                    "First (file)");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.isLoaded());
        client.uninstall("file-app");
	}
}
