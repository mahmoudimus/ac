package it;

import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.labs.remoteapps.test.webhook.WebHookBody;
import com.atlassian.labs.remoteapps.test.webhook.WebHookTester;
import com.atlassian.labs.remoteapps.test.webhook.WebHookWaiter;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static com.atlassian.labs.remoteapps.test.webhook.WebHookTestServlet.runInRunner;
import static java.util.Collections.singletonMap;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestFileInstall extends AbstractRemoteAppTest
{
    @Test
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

        RemoteAppInstallerClient client = new RemoteAppInstallerClient(
                baseUrl, "admin", "admin");
        client.install(descriptorFile.toURI().toString(), "", false);

        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "first",
                    "First (file)");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertTrue(remoteAppTest.getLoadTime() > 0);
        client.uninstall("file-app");
	}
}
